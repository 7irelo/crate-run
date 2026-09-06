use std::fs;
use std::io::{self, Read, Seek, SeekFrom, Write};
use std::path::Path;
use std::thread;
use std::time::Duration;

use anyhow::{bail, Context, Result};

use crate::cli::{Cli, Command};
use crate::core::model::{ContainerConfig, ContainerStatus};
use crate::core::state;
use crate::util::units;

/// Dispatch a parsed CLI command to the appropriate handler.
pub fn dispatch(cli: Cli) -> Result<()> {
    match cli.command {
        Command::Run {
            name,
            rootfs,
            memory,
            cpus,
            cpu,
            pids,
            uid,
            gid,
            hostname,
            cmd,
        } => {
            if let Some(name) = name.as_deref() {
                state::validate_name(name)?;
                state::ensure_name_available(name)?;
            }

            // --memory accepts human sizes; the cgroup file wants bytes.
            let memory = memory.as_deref().map(units::parse_size).transpose()?;

            // --cpus is a friendlier spelling of --cpu; clap already rejects
            // passing both.
            let cpu = match cpus {
                Some(cpus) => Some(units::cpus_to_cpu_max(cpus)?),
                None => cpu,
            };

            cmd_run(ContainerConfig {
                name,
                rootfs,
                cmd,
                hostname,
                memory,
                cpu,
                pids,
                uid,
                gid,
            })
        }
        Command::Ps => cmd_ps(),
        Command::Rm { id, force } => cmd_rm(&id, force),
        Command::Logs { id, follow } => cmd_logs(&id, follow),
        Command::Inspect { id } => cmd_inspect(&id),
        Command::Exec { id, cmd } => cmd_exec(&id, &cmd),
    }
}

// ─── run ────────────────────────────────────────────────────────────────────

fn cmd_run(config: ContainerConfig) -> Result<()> {
    #[cfg(not(target_os = "linux"))]
    {
        bail!("craterun only runs on Linux");
    }

    #[cfg(target_os = "linux")]
    {
        state::ensure_state_dir()?;

        let result = crate::platform::linux::process::run_container(&config)
            .context("failed to run container")?;

        println!("{}", result.container_id);
        std::process::exit(result.exit_code);
    }
}

// ─── ps ─────────────────────────────────────────────────────────────────────

fn cmd_ps() -> Result<()> {
    let ids = state::list_containers()?;

    println!(
        "{:<18} {:<14} {:<8} {:<10} {:<24} {}",
        "CONTAINER ID", "NAME", "PID", "STATUS", "CREATED", "COMMAND"
    );

    for id in ids {
        let mut meta = match state::load_meta(&id) {
            Ok(m) => m,
            Err(_) => continue,
        };
        state::refresh_status(&mut meta)?;

        let pid_str = if meta.pid > 0 {
            meta.pid.to_string()
        } else {
            "-".to_string()
        };

        let created = meta.created_at.format("%Y-%m-%d %H:%M:%S UTC");
        let cmd_str = meta.cmd.join(" ");
        let cmd_display = if cmd_str.len() > 40 {
            format!("{}...", &cmd_str[..37])
        } else {
            cmd_str
        };

        let name_display = meta.name.clone().unwrap_or_else(|| "-".to_string());

        println!(
            "{:<18} {:<14} {:<8} {:<10} {:<24} {}",
            &meta.id[..16.min(meta.id.len())],
            name_display,
            pid_str,
            meta.status,
            created,
            cmd_display
        );
    }

    Ok(())
}

// ─── rm ─────────────────────────────────────────────────────────────────────

fn cmd_rm(id_prefix: &str, force: bool) -> Result<()> {
    let id = state::resolve_ref(id_prefix)?;
    let mut meta = state::load_meta(&id)?;
    state::refresh_status(&mut meta)?;

    if meta.status == ContainerStatus::Running {
        if !force {
            bail!(
                "container {id} is still running. Use --force to remove a running container."
            );
        }
        // Kill the process first.
        #[cfg(target_os = "linux")]
        {
            crate::platform::linux::process::kill_container(meta.pid)?;
        }
    }

    // Remove cgroup.
    #[cfg(target_os = "linux")]
    {
        let _ = crate::platform::linux::cgroups::remove_cgroup(&id);
    }

    // Remove state directory.
    state::remove_container_dir(&id)?;

    println!("Removed container {id}");
    Ok(())
}

// ─── logs ───────────────────────────────────────────────────────────────────

fn cmd_logs(id_prefix: &str, follow: bool) -> Result<()> {
    let id = state::resolve_ref(id_prefix)?;

    let stdout_path = state::log_path(&id, state::STDOUT_LOG)?;
    let stderr_path = state::log_path(&id, state::STDERR_LOG)?;

    let mut stdout_offset = dump_from(&stdout_path, 0, false)?;
    let mut stderr_offset = dump_from(&stderr_path, 0, true)?;

    if !follow {
        return Ok(());
    }

    // Poll both log files, printing whatever has been appended since the last
    // pass. Following stops once the container is no longer running and there
    // is nothing further to read.
    loop {
        thread::sleep(FOLLOW_POLL_INTERVAL);

        stdout_offset = dump_from(&stdout_path, stdout_offset, false)?;
        stderr_offset = dump_from(&stderr_path, stderr_offset, true)?;

        let mut meta = state::load_meta(&id)?;
        state::refresh_status(&mut meta)?;
        if meta.status != ContainerStatus::Running {
            // One final drain, so output written as the process exited is not lost.
            dump_from(&stdout_path, stdout_offset, false)?;
            dump_from(&stderr_path, stderr_offset, true)?;
            return Ok(());
        }
    }
}

/// How often `--follow` re-reads the log files.
const FOLLOW_POLL_INTERVAL: Duration = Duration::from_millis(300);

/// Print everything in `path` beyond `offset`, returning the new offset.
///
/// A missing file is treated as empty, since a container may not have written
/// anything yet.
fn dump_from(path: &Path, offset: u64, to_stderr: bool) -> Result<u64> {
    if !path.exists() {
        return Ok(offset);
    }

    let mut file = fs::File::open(path)
        .with_context(|| format!("failed to open {}", path.display()))?;

    let len = file
        .metadata()
        .with_context(|| format!("failed to stat {}", path.display()))?
        .len();

    // A truncated or rotated file: start over rather than seek past the end.
    let start = if len < offset { 0 } else { offset };
    if len == start {
        return Ok(len);
    }

    file.seek(SeekFrom::Start(start))
        .with_context(|| format!("failed to seek in {}", path.display()))?;

    let mut buf = String::new();
    file.read_to_string(&mut buf)
        .with_context(|| format!("failed to read {}", path.display()))?;

    if !buf.is_empty() {
        if to_stderr {
            eprint!("{buf}");
            io::stderr().flush().ok();
        } else {
            print!("{buf}");
            io::stdout().flush().ok();
        }
    }

    Ok(len)
}

// ─── inspect ────────────────────────────────────────────────────────────────

fn cmd_inspect(id_prefix: &str) -> Result<()> {
    let id = state::resolve_ref(id_prefix)?;
    let mut meta = state::load_meta(&id)?;
    state::refresh_status(&mut meta)?;

    let json = serde_json::to_string_pretty(&meta)
        .context("failed to serialize container metadata")?;
    println!("{json}");

    Ok(())
}

// ─── exec ───────────────────────────────────────────────────────────────────

fn cmd_exec(id_prefix: &str, cmd: &[String]) -> Result<()> {
    let id = state::resolve_ref(id_prefix)?;
    let mut meta = state::load_meta(&id)?;
    state::refresh_status(&mut meta)?;

    if meta.status != ContainerStatus::Running {
        bail!("container {id} is not running");
    }

    #[cfg(not(target_os = "linux"))]
    {
        bail!("exec is only supported on Linux");
    }

    #[cfg(target_os = "linux")]
    {
        exec_in_container(meta.pid, cmd)?;
        Ok(())
    }
}

/// Enter the namespaces of a running container and exec a command.
#[cfg(target_os = "linux")]
fn exec_in_container(pid: u32, cmd: &[String]) -> Result<()> {
    use std::ffi::CString;

    if cmd.is_empty() {
        bail!("no command specified for exec");
    }

    // Open the namespaces of the target process.
    let ns_types = ["mnt", "pid", "uts", "ipc", "net"];
    let mut fds = Vec::new();

    for ns in &ns_types {
        let path = format!("/proc/{pid}/ns/{ns}");
        let file = fs::File::open(&path)
            .with_context(|| format!("failed to open namespace {path}"))?;
        fds.push((ns.to_string(), file));
    }

    // setns into each namespace.
    for (ns, file) in &fds {
        use std::os::unix::io::AsFd;
        nix::sched::setns(file.as_fd(), nix::sched::CloneFlags::empty()).with_context(|| {
            format!("failed to setns into {ns} namespace of pid {pid}")
        })?;
    }

    // chroot into the container's root.
    let root_path = format!("/proc/{pid}/root");
    nix::unistd::chroot(root_path.as_str())
        .context("failed to chroot into container root")?;
    nix::unistd::chdir("/").context("chdir / after chroot")?;

    // exec
    let program =
        CString::new(cmd[0].as_str()).with_context(|| format!("invalid command: {}", cmd[0]))?;
    let args: Vec<CString> = cmd
        .iter()
        .map(|a| CString::new(a.as_str()).context("invalid argument"))
        .collect::<Result<_>>()?;

    let env: Vec<CString> = vec![
        CString::new("PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin").unwrap(),
        CString::new("TERM=xterm").unwrap(),
    ];

    nix::unistd::execve(&program, &args, &env)
        .with_context(|| format!("execve '{}' failed", cmd[0]))?;

    unreachable!()
}

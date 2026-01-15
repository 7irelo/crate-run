use std::fs;

use anyhow::{bail, Context, Result};

use crate::cli::{Cli, Command};
use crate::core::model::{ContainerConfig, ContainerStatus};
use crate::core::state;

/// Dispatch a parsed CLI command to the appropriate handler.
pub fn dispatch(cli: Cli) -> Result<()> {
    match cli.command {
        Command::Run {
            rootfs,
            memory,
            cpu,
            pids,
            uid,
            gid,
            hostname,
            cmd,
        } => cmd_run(ContainerConfig {
            rootfs,
            cmd,
            hostname,
            memory,
            cpu,
            pids,
            uid,
            gid,
        }),
        Command::Ps => cmd_ps(),
        Command::Rm { id, force } => cmd_rm(&id, force),
        Command::Logs { id } => cmd_logs(&id),
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
        "{:<18} {:<8} {:<10} {:<24} {}",
        "CONTAINER ID", "PID", "STATUS", "CREATED", "COMMAND"
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

        println!(
            "{:<18} {:<8} {:<10} {:<24} {}",
            &meta.id[..16.min(meta.id.len())],
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
    let id = state::resolve_id(id_prefix)?;
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

fn cmd_logs(id_prefix: &str) -> Result<()> {
    let id = state::resolve_id(id_prefix)?;

    let stdout_path = state::log_path(&id, state::STDOUT_LOG)?;
    let stderr_path = state::log_path(&id, state::STDERR_LOG)?;

    if stdout_path.exists() {
        let contents =
            fs::read_to_string(&stdout_path).context("failed to read stdout.log")?;
        if !contents.is_empty() {
            print!("{contents}");
        }
    }

    if stderr_path.exists() {
        let contents =
            fs::read_to_string(&stderr_path).context("failed to read stderr.log")?;
        if !contents.is_empty() {
            eprint!("{contents}");
        }
    }

    Ok(())
}

// ─── exec ───────────────────────────────────────────────────────────────────

fn cmd_exec(id_prefix: &str, cmd: &[String]) -> Result<()> {
    let id = state::resolve_id(id_prefix)?;
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

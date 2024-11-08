use std::ffi::CString;
use std::fs::{self, File};
use std::io::Read;
use std::os::unix::io::{AsRawFd, FromRawFd, IntoRawFd};
use std::path::Path;

use anyhow::{bail, Context, Result};
use nix::sys::signal::Signal;
use nix::sys::wait::{waitpid, WaitStatus};
use nix::unistd::{self, ForkResult, Pid};

use crate::core::model::ContainerConfig;
use crate::core::state;
use crate::platform::linux::{cgroups, mounts, namespaces};

/// Outcome of running a container.
pub struct RunResult {
    /// The container's assigned ID.
    pub container_id: String,
    /// The exit code of the container's init process (or 128+signal for signal death).
    pub exit_code: i32,
}

/// Launch a container: fork, unshare, setup mounts/cgroups, exec.
///
/// # Safety
///
/// This function calls `fork()`. The child performs `exec`. This is safe as
/// long as no other threads are running at fork time — we call this very early.
pub fn run_container(config: &ContainerConfig) -> Result<RunResult> {
    validate_rootfs(&config.rootfs)?;

    let container_id = crate::core::id::generate_id();
    let rootfs = fs::canonicalize(&config.rootfs)
        .with_context(|| format!("failed to canonicalize rootfs path '{}'", config.rootfs))?;

    // Create log files before forking.
    let container_dir = state::container_dir(&container_id)?;
    fs::create_dir_all(&container_dir)?;
    let stdout_file = File::create(container_dir.join(state::STDOUT_LOG))
        .context("failed to create stdout.log")?;
    let stderr_file = File::create(container_dir.join(state::STDERR_LOG))
        .context("failed to create stderr.log")?;

    // Set up a pipe for the child to signal readiness / report errors.
    // pipe() returns (read_end, write_end) as OwnedFd.
    let (read_fd, write_fd) = nix::unistd::pipe().context("failed to create pipe")?;

    // Convert OwnedFds to raw fds immediately. We manage lifetime manually
    // across the fork boundary — OwnedFd drop semantics don't work across fork.
    let read_raw = read_fd.into_raw_fd();
    let write_raw = write_fd.into_raw_fd();

    // SAFETY: We fork here. The child will exec or _exit.
    match unsafe { unistd::fork() }.context("fork failed")? {
        ForkResult::Parent { child } => {
            // Close write end in parent.
            unsafe { libc::close(write_raw) };
            // Wrap read end in a File (takes ownership).
            let reader = unsafe { File::from_raw_fd(read_raw) };
            parent_process(child, &container_id, config, reader)
        }
        ForkResult::Child => {
            // Close read end in child.
            unsafe { libc::close(read_raw) };
            // In the child: any error is sent via the pipe before _exit(1).
            let result =
                child_process(config, &rootfs, &container_id, &stdout_file, &stderr_file);
            if let Err(e) = &result {
                let msg = format!("{e:#}");
                let _ = unsafe { libc::write(write_raw, msg.as_ptr() as *const _, msg.len()) };
            }
            // Close write end to signal parent (EOF on read end).
            unsafe { libc::close(write_raw) };
            std::process::exit(1);
        }
    }
}

fn parent_process(
    child: Pid,
    container_id: &str,
    config: &ContainerConfig,
    mut reader: File,
) -> Result<RunResult> {
    // Read any error message from the child through the pipe.
    let mut buf = String::new();
    reader.read_to_string(&mut buf).ok();
    drop(reader);

    if !buf.is_empty() {
        bail!("container child setup failed: {buf}");
    }

    // Save metadata.
    let meta = crate::core::model::ContainerMeta {
        id: container_id.to_string(),
        rootfs: config.rootfs.clone(),
        cmd: config.cmd.clone(),
        pid: child.as_raw() as u32,
        exit_code: None,
        created_at: chrono::Utc::now(),
        status: crate::core::model::ContainerStatus::Running,
        hostname: config.hostname.clone(),
        memory_limit: config.memory,
        cpu_limit: config.cpu.clone(),
        pids_limit: config.pids,
    };
    state::save_meta(&meta)?;

    // Wait for the child.
    let exit_code = wait_for_child(child)?;

    // Update metadata.
    let mut meta = state::load_meta(container_id)?;
    meta.status = crate::core::model::ContainerStatus::Stopped;
    meta.exit_code = Some(exit_code);
    meta.pid = 0;
    state::save_meta(&meta)?;

    // Clean up cgroup.
    let _ = cgroups::remove_cgroup(container_id);

    Ok(RunResult {
        container_id: container_id.to_string(),
        exit_code,
    })
}

fn child_process(
    config: &ContainerConfig,
    rootfs: &Path,
    container_id: &str,
    stdout_file: &File,
    stderr_file: &File,
) -> Result<()> {
    // 1. Unshare namespaces.
    let flags = namespaces::container_clone_flags();
    namespaces::unshare_namespaces(flags)?;

    // 2. Set up cgroup and place ourselves into it BEFORE fork into PID namespace.
    let cg_path = cgroups::setup_cgroup(
        container_id,
        config.memory,
        config.cpu.as_deref(),
        config.pids,
    )?;
    cgroups::add_process(&cg_path, std::process::id())?;

    // 3. Fork again to enter the PID namespace (the child of this fork gets PID 1).
    match unsafe { unistd::fork() }.context("inner fork (pid namespace) failed")? {
        ForkResult::Parent { child } => {
            // Wait for the grandchild (container init).
            let status = waitpid(child, None).context("waitpid on container init")?;
            let code = match status {
                WaitStatus::Exited(_, c) => c,
                WaitStatus::Signaled(_, sig, _) => 128 + sig as i32,
                _ => 1,
            };
            std::process::exit(code);
        }
        ForkResult::Child => {
            // This is PID 1 inside the new PID namespace.
            init_container(config, rootfs, stdout_file, stderr_file)?;
            unreachable!("exec should have replaced this process");
        }
    }
}

fn init_container(
    config: &ContainerConfig,
    rootfs: &Path,
    stdout_file: &File,
    stderr_file: &File,
) -> Result<()> {
    // Set hostname.
    namespaces::set_hostname(&config.hostname)?;

    // Mount setup: make tree private, bind-mount rootfs, mount /proc, pivot_root.
    mounts::make_mount_private()?;
    mounts::bind_mount_rootfs(rootfs)?;
    mounts::mount_proc(rootfs)?;
    mounts::pivot_root(rootfs)?;
    mounts::mount_proc_in_new_root()?;
    mounts::mount_dev_in_new_root()?;

    // Redirect stdout/stderr to log files.
    nix::unistd::dup2(stdout_file.as_raw_fd(), 1).context("dup2 stdout")?;
    nix::unistd::dup2(stderr_file.as_raw_fd(), 2).context("dup2 stderr")?;

    // Exec the user command.
    let cmd = &config.cmd;
    if cmd.is_empty() {
        bail!("no command specified");
    }

    let program = CString::new(cmd[0].as_str())
        .with_context(|| format!("invalid command: '{}'", cmd[0]))?;
    let args: Vec<CString> = cmd
        .iter()
        .map(|a| CString::new(a.as_str()).context("invalid argument"))
        .collect::<Result<_>>()?;

    // Set minimal environment.
    let env: Vec<CString> = vec![
        CString::new("PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin").unwrap(),
        CString::new(format!("HOSTNAME={}", config.hostname)).unwrap(),
        CString::new("TERM=xterm").unwrap(),
        CString::new("HOME=/root").unwrap(),
    ];

    nix::unistd::execve(&program, &args, &env)
        .with_context(|| format!("execve '{}' failed", cmd[0]))?;

    unreachable!();
}

/// Wait for a child process and return its exit code.
fn wait_for_child(pid: Pid) -> Result<i32> {
    loop {
        match waitpid(pid, None) {
            Ok(WaitStatus::Exited(_, code)) => return Ok(code),
            Ok(WaitStatus::Signaled(_, sig, _)) => return Ok(128 + sig as i32),
            Ok(_) => continue,
            Err(nix::errno::Errno::EINTR) => continue,
            Err(e) => bail!("waitpid failed: {e}"),
        }
    }
}

/// Validate that the rootfs path is safe and looks correct.
fn validate_rootfs(rootfs: &str) -> Result<()> {
    if rootfs.is_empty() {
        bail!("rootfs path must not be empty");
    }

    let path = Path::new(rootfs);

    // Refuse dangerous paths.
    let canon = if path.exists() {
        fs::canonicalize(path)
            .with_context(|| format!("cannot canonicalize rootfs path '{rootfs}'"))?
    } else {
        bail!("rootfs path '{rootfs}' does not exist");
    };

    if canon == Path::new("/") {
        bail!("refusing to use '/' as rootfs — this would destroy the host");
    }

    // Check it looks like a filesystem root (has bin/ or usr/ or etc/).
    let looks_like_root = canon.join("bin").is_dir()
        || canon.join("usr").is_dir()
        || canon.join("etc").is_dir();

    if !looks_like_root {
        bail!(
            "rootfs '{}' does not look like a filesystem root (no bin/, usr/, or etc/ found). \
             Please provide a path to an extracted rootfs (e.g. Alpine minirootfs).",
            canon.display()
        );
    }

    Ok(())
}

/// Send SIGKILL to a running container process.
pub fn kill_container(pid: u32) -> Result<()> {
    if pid == 0 {
        return Ok(());
    }
    let pid = Pid::from_raw(pid as i32);
    nix::sys::signal::kill(pid, Signal::SIGKILL)
        .with_context(|| format!("failed to kill process {pid}"))?;
    // Wait briefly for it to die.
    let _ = waitpid(pid, None);
    Ok(())
}

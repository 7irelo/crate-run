use anyhow::{Context, Result};
use nix::sched::CloneFlags;

/// Return the set of namespace flags we want for a new container.
///
/// We use: mount, pid, UTS, IPC, and network.
/// Network namespace isolation is included; the container gets a new, empty
/// network stack (loopback only). If you need host networking pass `--net=host`
/// in a future version.
pub fn container_clone_flags() -> CloneFlags {
    CloneFlags::CLONE_NEWNS
        | CloneFlags::CLONE_NEWPID
        | CloneFlags::CLONE_NEWUTS
        | CloneFlags::CLONE_NEWIPC
        | CloneFlags::CLONE_NEWNET
}

/// Call `unshare(2)` with the given flags. Used when we fork first and then
/// unshare in the child.
pub fn unshare_namespaces(flags: CloneFlags) -> Result<()> {
    nix::sched::unshare(flags).context("unshare failed — are you running as root?")?;
    Ok(())
}

/// Set the hostname inside a UTS namespace.
pub fn set_hostname(name: &str) -> Result<()> {
    nix::unistd::sethostname(name).context("sethostname failed")?;
    Ok(())
}

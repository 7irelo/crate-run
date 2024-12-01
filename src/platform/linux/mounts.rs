use std::fs;
use std::path::Path;

use anyhow::{Context, Result};
use nix::mount::{mount, umount2, MntFlags, MsFlags};

/// Make the entire mount tree private so our changes do not leak to the host.
pub fn make_mount_private() -> Result<()> {
    mount(
        None::<&str>,
        "/",
        None::<&str>,
        MsFlags::MS_REC | MsFlags::MS_PRIVATE,
        None::<&str>,
    )
    .context("failed to make / private recursively")?;
    Ok(())
}

/// Bind-mount the rootfs onto itself so it becomes a mount point
/// (required for `pivot_root`).
pub fn bind_mount_rootfs(rootfs: &Path) -> Result<()> {
    mount(
        Some(rootfs),
        rootfs,
        None::<&str>,
        MsFlags::MS_BIND | MsFlags::MS_REC,
        None::<&str>,
    )
    .with_context(|| format!("failed to bind-mount rootfs {}", rootfs.display()))?;
    Ok(())
}

/// Perform `pivot_root` to make `new_root` the new `/` and put the old root under
/// `new_root/.pivot_old`. Then unmount and remove the old root.
pub fn pivot_root(new_root: &Path) -> Result<()> {
    let put_old = new_root.join(".pivot_old");
    fs::create_dir_all(&put_old)
        .with_context(|| format!("failed to create {}", put_old.display()))?;

    nix::unistd::pivot_root(new_root, &put_old).with_context(|| {
        format!(
            "pivot_root({}, {}) failed",
            new_root.display(),
            put_old.display()
        )
    })?;

    // After pivot_root, `/.pivot_old` is the old root.
    nix::unistd::chdir("/").context("chdir / after pivot_root")?;

    umount_old_root("/.pivot_old")?;
    Ok(())
}

/// Unmount the old root and remove the directory.
fn umount_old_root(path: &str) -> Result<()> {
    umount2(path, MntFlags::MNT_DETACH)
        .with_context(|| format!("failed to unmount old root at {path}"))?;
    fs::remove_dir(path)
        .with_context(|| format!("failed to remove old root directory {path}"))?;
    Ok(())
}

/// Mount `/proc` inside the new root.
pub fn mount_proc(rootfs: &Path) -> Result<()> {
    let proc_dir = rootfs.join("proc");
    fs::create_dir_all(&proc_dir)
        .with_context(|| format!("failed to create {}", proc_dir.display()))?;

    mount(
        Some("proc"),
        &proc_dir,
        Some("proc"),
        MsFlags::MS_NOSUID | MsFlags::MS_NODEV | MsFlags::MS_NOEXEC,
        None::<&str>,
    )
    .with_context(|| format!("failed to mount proc at {}", proc_dir.display()))?;
    Ok(())
}

/// Mount `/proc` at `/proc` (used after pivot_root when `/` is already the new root).
pub fn mount_proc_in_new_root() -> Result<()> {
    let proc_dir = Path::new("/proc");
    fs::create_dir_all(proc_dir).context("failed to create /proc")?;

    mount(
        Some("proc"),
        proc_dir,
        Some("proc"),
        MsFlags::MS_NOSUID | MsFlags::MS_NODEV | MsFlags::MS_NOEXEC,
        None::<&str>,
    )
    .context("failed to mount proc at /proc")?;
    Ok(())
}

/// Mount a minimal `/dev` with devtmpfs.
pub fn mount_dev_in_new_root() -> Result<()> {
    let dev_dir = Path::new("/dev");
    fs::create_dir_all(dev_dir).context("failed to create /dev")?;

    mount(
        Some("tmpfs"),
        dev_dir,
        Some("tmpfs"),
        MsFlags::MS_NOSUID,
        Some("mode=0755,size=65536k"),
    )
    .context("failed to mount tmpfs on /dev")?;

    // Create essential device nodes (null, zero, urandom, tty).
    create_dev_nodes()?;

    Ok(())
}

/// Create minimal device nodes inside the container's /dev.
fn create_dev_nodes() -> Result<()> {
    use nix::sys::stat;

    let perm = stat::Mode::from_bits_truncate(0o666);
    let devices = [
        ("/dev/null", nix::sys::stat::makedev(1, 3)),
        ("/dev/zero", nix::sys::stat::makedev(1, 5)),
        ("/dev/urandom", nix::sys::stat::makedev(1, 9)),
        ("/dev/tty", nix::sys::stat::makedev(5, 0)),
    ];

    for (path, dev) in &devices {
        // mknod may fail if not root or if devtmpfs already provides it; ignore error.
        let _ = stat::mknod(Path::new(path), stat::SFlag::S_IFCHR, perm, *dev);
    }

    Ok(())
}

use std::fs;
use std::path::{Path, PathBuf};

use anyhow::{bail, Context, Result};

/// The cgroup v2 unified mount point.
const CGROUP_ROOT: &str = "/sys/fs/cgroup";
/// CrateRun puts all its cgroups under this sub-hierarchy.
const CRATERUN_PREFIX: &str = "craterun";

/// Return the cgroup path for a specific container (e.g.
/// `/sys/fs/cgroup/craterun/<container_id>`).
pub fn cgroup_path(container_id: &str) -> PathBuf {
    Path::new(CGROUP_ROOT)
        .join(CRATERUN_PREFIX)
        .join(container_id)
}

/// Create a cgroup for the container and apply resource limits.
pub fn setup_cgroup(
    container_id: &str,
    memory: Option<u64>,
    cpu: Option<&str>,
    pids: Option<u64>,
) -> Result<PathBuf> {
    let path = cgroup_path(container_id);

    // Ensure parent "craterun" cgroup exists
    let parent = path.parent().unwrap();
    if !parent.exists() {
        fs::create_dir_all(parent).with_context(|| {
            format!(
                "failed to create parent cgroup dir {}. Is cgroups v2 mounted?",
                parent.display()
            )
        })?;
        // Enable controllers in the parent so children can use them.
        enable_controllers(parent)?;
    }

    fs::create_dir_all(&path)
        .with_context(|| format!("failed to create cgroup {}", path.display()))?;

    if let Some(mem) = memory {
        write_cgroup_file(&path, "memory.max", &mem.to_string())
            .context("failed to set memory.max")?;
    }

    if let Some(cpu_max) = cpu {
        write_cgroup_file(&path, "cpu.max", cpu_max).context("failed to set cpu.max")?;
    }

    if let Some(max_pids) = pids {
        write_cgroup_file(&path, "pids.max", &max_pids.to_string())
            .context("failed to set pids.max")?;
    }

    Ok(path)
}

/// Place a process into a cgroup by writing its PID to `cgroup.procs`.
pub fn add_process(cgroup: &Path, pid: u32) -> Result<()> {
    write_cgroup_file(cgroup, "cgroup.procs", &pid.to_string())
        .with_context(|| format!("failed to add pid {pid} to cgroup {}", cgroup.display()))
}

/// Remove the cgroup directory (must be empty of processes first).
pub fn remove_cgroup(container_id: &str) -> Result<()> {
    let path = cgroup_path(container_id);
    if path.exists() {
        // The cgroup may still have zombie references; try to remove.
        fs::remove_dir(&path).with_context(|| {
            format!(
                "failed to remove cgroup {}. Is the container still running?",
                path.display()
            )
        })?;
    }
    Ok(())
}

/// Enable all available controllers in a cgroup (write to `cgroup.subtree_control`).
fn enable_controllers(path: &Path) -> Result<()> {
    let controllers_file = path.join("cgroup.controllers");
    if !controllers_file.exists() {
        return Ok(());
    }

    let available = fs::read_to_string(&controllers_file)
        .with_context(|| format!("failed to read {}", controllers_file.display()))?;

    let enable_str: String = available
        .split_whitespace()
        .map(|c| format!("+{c}"))
        .collect::<Vec<_>>()
        .join(" ");

    if !enable_str.is_empty() {
        let subtree = path.join("cgroup.subtree_control");
        fs::write(&subtree, &enable_str).with_context(|| {
            format!(
                "failed to enable controllers ({enable_str}) in {}",
                subtree.display()
            )
        })?;
    }

    Ok(())
}

/// Write a value to a cgroup control file.
fn write_cgroup_file(cgroup: &Path, filename: &str, value: &str) -> Result<()> {
    let file = cgroup.join(filename);
    if !cgroup.exists() {
        bail!("cgroup directory {} does not exist", cgroup.display());
    }
    fs::write(&file, value)
        .with_context(|| format!("failed to write '{value}' to {}", file.display()))?;
    Ok(())
}

use std::fs;
use std::path::{Path, PathBuf};

use anyhow::{bail, Context, Result};

use super::model::{ContainerMeta, ContainerStatus};

/// Name of the per-container metadata file.
const META_FILE: &str = "metadata.json";
/// Name of the stdout log file.
pub const STDOUT_LOG: &str = "stdout.log";
/// Name of the stderr log file.
pub const STDERR_LOG: &str = "stderr.log";

/// Return the base state directory.
///
/// When running as root (`euid == 0`), use `/var/lib/craterun`.
/// Otherwise use `$HOME/.craterun`.
pub fn state_dir() -> Result<PathBuf> {
    #[cfg(target_os = "linux")]
    {
        if nix::unistd::geteuid().is_root() {
            return Ok(PathBuf::from("/var/lib/craterun"));
        }
    }

    let home = std::env::var("HOME").context("HOME environment variable not set")?;
    Ok(PathBuf::from(home).join(".craterun"))
}

/// Return the directory for a specific container.
pub fn container_dir(id: &str) -> Result<PathBuf> {
    Ok(state_dir()?.join(id))
}

/// Ensure the base state directory exists.
pub fn ensure_state_dir() -> Result<PathBuf> {
    let dir = state_dir()?;
    fs::create_dir_all(&dir)
        .with_context(|| format!("failed to create state directory {}", dir.display()))?;
    Ok(dir)
}

/// Save container metadata to disk.
pub fn save_meta(meta: &ContainerMeta) -> Result<()> {
    let dir = container_dir(&meta.id)?;
    fs::create_dir_all(&dir)
        .with_context(|| format!("failed to create container directory {}", dir.display()))?;

    let path = dir.join(META_FILE);
    let json = serde_json::to_string_pretty(meta).context("failed to serialize metadata")?;
    fs::write(&path, json)
        .with_context(|| format!("failed to write metadata to {}", path.display()))?;
    Ok(())
}

/// Load container metadata from disk.
pub fn load_meta(id: &str) -> Result<ContainerMeta> {
    let path = container_dir(id)?.join(META_FILE);
    let data = fs::read_to_string(&path)
        .with_context(|| format!("failed to read metadata from {}", path.display()))?;
    let meta: ContainerMeta =
        serde_json::from_str(&data).context("failed to parse container metadata")?;
    Ok(meta)
}

/// List all container IDs in the state directory.
pub fn list_containers() -> Result<Vec<String>> {
    let dir = match state_dir() {
        Ok(d) => d,
        Err(_) => return Ok(Vec::new()),
    };
    if !dir.exists() {
        return Ok(Vec::new());
    }

    let mut ids = Vec::new();
    for entry in
        fs::read_dir(&dir).with_context(|| format!("failed to read {}", dir.display()))?
    {
        let entry = entry?;
        if entry.path().join(META_FILE).exists() {
            if let Some(name) = entry.file_name().to_str() {
                ids.push(name.to_string());
            }
        }
    }
    ids.sort();
    Ok(ids)
}

/// Resolve a potentially abbreviated container ID to a full ID.
///
/// If `prefix` matches exactly one container, return that container's full ID.
/// If multiple match, return an error listing the ambiguous matches.
pub fn resolve_id(prefix: &str) -> Result<String> {
    let all = list_containers()?;
    let matches: Vec<&String> = all.iter().filter(|id| id.starts_with(prefix)).collect();

    match matches.len() {
        0 => bail!("no container found with ID prefix '{prefix}'"),
        1 => Ok(matches[0].clone()),
        n => {
            let preview: Vec<&str> = matches.iter().take(5).map(|s| s.as_str()).collect();
            bail!(
                "ambiguous container ID prefix '{prefix}': {n} matches ({})",
                preview.join(", ")
            );
        }
    }
}

/// Remove the state directory for a container.
pub fn remove_container_dir(id: &str) -> Result<()> {
    let dir = container_dir(id)?;
    if dir.exists() {
        fs::remove_dir_all(&dir).with_context(|| {
            format!(
                "failed to remove container directory {}",
                dir.display()
            )
        })?;
    }
    Ok(())
}

/// Return the path for stdout or stderr log.
pub fn log_path(id: &str, name: &str) -> Result<PathBuf> {
    Ok(container_dir(id)?.join(name))
}

/// Check whether a PID is alive on the host.
pub fn pid_alive(pid: u32) -> bool {
    if pid == 0 {
        return false;
    }
    Path::new(&format!("/proc/{pid}")).exists()
}

/// Refresh the status field of metadata based on whether the PID is still alive.
/// Returns `true` if the status was changed and saved.
pub fn refresh_status(meta: &mut ContainerMeta) -> Result<bool> {
    if meta.status == ContainerStatus::Running && !pid_alive(meta.pid) {
        meta.status = ContainerStatus::Stopped;
        save_meta(meta)?;
        return Ok(true);
    }
    Ok(false)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::core::model::{ContainerMeta, ContainerStatus};
    use chrono::Utc;
    use std::env;

    /// Helper: set HOME to a temp directory so state goes there.
    fn with_tmp_home(dir: &Path) {
        env::set_var("HOME", dir.to_str().unwrap());
    }

    fn sample_meta(id: &str) -> ContainerMeta {
        ContainerMeta {
            id: id.into(),
            rootfs: "/tmp/rootfs".into(),
            cmd: vec!["/bin/sh".into()],
            pid: 0,
            exit_code: None,
            created_at: Utc::now(),
            status: ContainerStatus::Stopped,
            hostname: "craterun".into(),
            memory_limit: None,
            cpu_limit: None,
            pids_limit: None,
        }
    }

    #[test]
    fn save_and_load_meta() {
        let tmp = tempfile::tempdir().unwrap();
        with_tmp_home(tmp.path());

        let meta = sample_meta("aabbccdd11223344");
        save_meta(&meta).unwrap();
        let loaded = load_meta("aabbccdd11223344").unwrap();
        assert_eq!(loaded.id, meta.id);
        assert_eq!(loaded.rootfs, meta.rootfs);
    }

    #[test]
    fn list_and_resolve_containers() {
        let tmp = tempfile::tempdir().unwrap();
        with_tmp_home(tmp.path());

        save_meta(&sample_meta("aabbccdd11223344")).unwrap();
        save_meta(&sample_meta("aabbccdd55667788")).unwrap();
        save_meta(&sample_meta("11223344aabbccdd")).unwrap();

        let all = list_containers().unwrap();
        assert_eq!(all.len(), 3);

        // Exact match prefix
        let id = resolve_id("11223344aabbccdd").unwrap();
        assert_eq!(id, "11223344aabbccdd");

        // Unique prefix
        let id = resolve_id("1122").unwrap();
        assert_eq!(id, "11223344aabbccdd");

        // Ambiguous prefix
        assert!(resolve_id("aabb").is_err());

        // No match
        assert!(resolve_id("ffff").is_err());
    }

    #[test]
    fn remove_container() {
        let tmp = tempfile::tempdir().unwrap();
        with_tmp_home(tmp.path());

        save_meta(&sample_meta("deadbeef12345678")).unwrap();
        assert!(list_containers().unwrap().contains(&"deadbeef12345678".to_string()));

        remove_container_dir("deadbeef12345678").unwrap();
        assert!(!list_containers().unwrap().contains(&"deadbeef12345678".to_string()));
    }
}

use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use std::fmt;

/// Status of a container in the CrateRun runtime.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "lowercase")]
pub enum ContainerStatus {
    /// The container process is believed to be running.
    Running,
    /// The container process has exited.
    Stopped,
    /// The container was created but never started (should not normally persist).
    Created,
}

impl fmt::Display for ContainerStatus {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Running => write!(f, "running"),
            Self::Stopped => write!(f, "stopped"),
            Self::Created => write!(f, "created"),
        }
    }
}

/// Persisted metadata for a single container.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ContainerMeta {
    /// Unique hex container ID.
    pub id: String,
    /// Absolute path to the root filesystem.
    pub rootfs: String,
    /// The command (and arguments) the container was started with.
    pub cmd: Vec<String>,
    /// PID of the container init process on the host (0 if not running).
    pub pid: u32,
    /// Exit code of the container process, if exited.
    pub exit_code: Option<i32>,
    /// When the container was created.
    pub created_at: DateTime<Utc>,
    /// Current status.
    pub status: ContainerStatus,
    /// Hostname set inside the container.
    pub hostname: String,
    /// Memory limit in bytes, if set.
    pub memory_limit: Option<u64>,
    /// CPU limit string for cpu.max, if set.
    pub cpu_limit: Option<String>,
    /// PID limit, if set.
    pub pids_limit: Option<u64>,
}

/// Configuration for launching a new container. Constructed from CLI arguments.
#[derive(Debug, Clone)]
pub struct ContainerConfig {
    pub rootfs: String,
    pub cmd: Vec<String>,
    pub hostname: String,
    pub memory: Option<u64>,
    pub cpu: Option<String>,
    pub pids: Option<u64>,
    pub uid: Option<u32>,
    pub gid: Option<u32>,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn status_display() {
        assert_eq!(ContainerStatus::Running.to_string(), "running");
        assert_eq!(ContainerStatus::Stopped.to_string(), "stopped");
        assert_eq!(ContainerStatus::Created.to_string(), "created");
    }

    #[test]
    fn meta_serialization_round_trip() {
        let meta = ContainerMeta {
            id: "abcdef0123456789".into(),
            rootfs: "/tmp/rootfs".into(),
            cmd: vec!["/bin/sh".into(), "-c".into(), "echo hi".into()],
            pid: 12345,
            exit_code: None,
            created_at: Utc::now(),
            status: ContainerStatus::Running,
            hostname: "craterun".into(),
            memory_limit: Some(67108864),
            cpu_limit: None,
            pids_limit: Some(100),
        };

        let json = serde_json::to_string(&meta).expect("serialize");
        let back: ContainerMeta = serde_json::from_str(&json).expect("deserialize");
        assert_eq!(back.id, meta.id);
        assert_eq!(back.rootfs, meta.rootfs);
        assert_eq!(back.cmd, meta.cmd);
        assert_eq!(back.pid, meta.pid);
        assert_eq!(back.status, meta.status);
        assert_eq!(back.memory_limit, Some(67108864));
    }
}

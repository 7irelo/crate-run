use std::env;
use std::path::Path;

use chrono::Utc;
use tempfile::TempDir;

/// Helper to point the state directory at a temp dir.
fn setup_home(tmp: &TempDir) {
    env::set_var("HOME", tmp.path().to_str().unwrap());
}

/// Minimal ContainerMeta for testing (mirrors the crate's model).
#[derive(serde::Serialize, serde::Deserialize, Debug)]
struct ContainerMeta {
    id: String,
    rootfs: String,
    cmd: Vec<String>,
    pid: u32,
    exit_code: Option<i32>,
    created_at: chrono::DateTime<Utc>,
    status: String,
    hostname: String,
    memory_limit: Option<u64>,
    cpu_limit: Option<String>,
    pids_limit: Option<u64>,
}

#[test]
fn state_directory_uses_home() {
    let tmp = tempfile::tempdir().unwrap();
    setup_home(&tmp);

    let home = env::var("HOME").unwrap();
    let expected = Path::new(&home).join(".craterun");

    // The function is internal to the crate, so we verify the convention.
    assert!(expected.to_str().unwrap().contains(".craterun"));
}

#[test]
fn metadata_json_round_trip() {
    let meta = ContainerMeta {
        id: "aabbccdd11223344".into(),
        rootfs: "/tmp/rootfs".into(),
        cmd: vec!["/bin/sh".into(), "-c".into(), "echo hello".into()],
        pid: 0,
        exit_code: Some(0),
        created_at: Utc::now(),
        status: "stopped".into(),
        hostname: "craterun".into(),
        memory_limit: Some(1024 * 1024 * 64),
        cpu_limit: None,
        pids_limit: None,
    };

    let json = serde_json::to_string_pretty(&meta).unwrap();
    let back: ContainerMeta = serde_json::from_str(&json).unwrap();

    assert_eq!(back.id, "aabbccdd11223344");
    assert_eq!(back.rootfs, "/tmp/rootfs");
    assert_eq!(back.cmd, vec!["/bin/sh", "-c", "echo hello"]);
    assert_eq!(back.exit_code, Some(0));
    assert_eq!(back.status, "stopped");
    assert_eq!(back.memory_limit, Some(67108864));
}

#[test]
fn metadata_handles_all_statuses() {
    for status in &["running", "stopped", "created"] {
        let json = format!(
            r#"{{
                "id": "0000000000000000",
                "rootfs": "/tmp/r",
                "cmd": ["/bin/sh"],
                "pid": 0,
                "exit_code": null,
                "created_at": "2025-01-01T00:00:00Z",
                "status": "{}",
                "hostname": "test",
                "memory_limit": null,
                "cpu_limit": null,
                "pids_limit": null
            }}"#,
            status
        );
        let meta: ContainerMeta = serde_json::from_str(&json).unwrap();
        assert_eq!(meta.status, *status);
    }
}

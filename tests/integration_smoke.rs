/// Integration smoke test for CrateRun.
///
/// This test requires:
/// 1. Running on Linux.
/// 2. Running as root (or with sufficient privileges for namespaces + cgroups).
/// 3. An Alpine minirootfs extracted at `tests/rootfs/` (or the path set in
///    `CRATERUN_TEST_ROOTFS`).
///
/// In CI, the workflow downloads and extracts the rootfs before running tests.
/// Locally, you can prepare it with:
///
/// ```bash
/// mkdir -p tests/rootfs
/// curl -L https://dl-cdn.alpinelinux.org/alpine/v3.20/releases/x86_64/alpine-minirootfs-3.20.3-x86_64.tar.gz \
///     | tar -xz -C tests/rootfs
/// ```
///
/// The test is skipped if not running as root or if the rootfs is missing.

use std::path::Path;
use std::process::Command;

/// Return the rootfs path to use for integration tests.
fn rootfs_path() -> String {
    std::env::var("CRATERUN_TEST_ROOTFS")
        .unwrap_or_else(|_| "tests/rootfs".to_string())
}

/// Check whether we can run integration tests.
fn can_run() -> bool {
    // Must be on Linux.
    if cfg!(not(target_os = "linux")) {
        eprintln!("SKIP: not on Linux");
        return false;
    }

    // Must be root.
    if !nix_is_root() {
        eprintln!("SKIP: not running as root (euid != 0)");
        return false;
    }

    // Must have rootfs.
    let rfs = rootfs_path();
    if !Path::new(&rfs).join("bin").exists() {
        eprintln!("SKIP: rootfs not found at {rfs}/bin");
        return false;
    }

    true
}

fn nix_is_root() -> bool {
    #[cfg(target_os = "linux")]
    {
        nix::unistd::geteuid().is_root()
    }
    #[cfg(not(target_os = "linux"))]
    {
        false
    }
}

#[test]
fn smoke_echo() {
    if !can_run() {
        eprintln!("Skipping integration test (prerequisites not met)");
        return;
    }

    let rootfs = rootfs_path();
    let tmp_home = tempfile::tempdir().unwrap();

    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args([
            "run",
            "--rootfs",
            &rootfs,
            "--",
            "/bin/sh",
            "-c",
            "echo hi",
        ])
        .env("HOME", tmp_home.path())
        .output()
        .expect("failed to run craterun");

    let stdout = String::from_utf8_lossy(&output.stdout);
    let stderr = String::from_utf8_lossy(&output.stderr);

    eprintln!("--- stdout ---\n{stdout}");
    eprintln!("--- stderr ---\n{stderr}");

    assert!(
        output.status.success(),
        "craterun run should succeed, exit code: {:?}, stderr: {stderr}",
        output.status.code()
    );

    // The container ID is printed to stdout. Logs go to files.
    // Verify there's a container ID (16 hex chars) on the first line.
    let first_line = stdout.lines().next().unwrap_or("");
    assert!(
        first_line.len() >= 16
            && first_line
                .chars()
                .all(|c| c.is_ascii_hexdigit()),
        "expected container ID on first line, got: '{first_line}'"
    );

    // Now check the logs contain "hi".
    let container_id = first_line.trim();
    let log_output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args(["logs", container_id])
        .env("HOME", tmp_home.path())
        .output()
        .expect("failed to run craterun logs");

    let log_stdout = String::from_utf8_lossy(&log_output.stdout);
    assert!(
        log_stdout.contains("hi"),
        "logs should contain 'hi', got: '{log_stdout}'"
    );
}

#[test]
fn smoke_exit_code_propagation() {
    if !can_run() {
        eprintln!("Skipping integration test (prerequisites not met)");
        return;
    }

    let rootfs = rootfs_path();
    let tmp_home = tempfile::tempdir().unwrap();

    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args([
            "run",
            "--rootfs",
            &rootfs,
            "--",
            "/bin/sh",
            "-c",
            "exit 42",
        ])
        .env("HOME", tmp_home.path())
        .output()
        .expect("failed to run craterun");

    assert_eq!(
        output.status.code(),
        Some(42),
        "exit code should be propagated from container"
    );
}

#[test]
fn smoke_ps_shows_stopped() {
    if !can_run() {
        eprintln!("Skipping integration test (prerequisites not met)");
        return;
    }

    let rootfs = rootfs_path();
    let tmp_home = tempfile::tempdir().unwrap();

    // Run a container.
    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args(["run", "--rootfs", &rootfs, "--", "/bin/true"])
        .env("HOME", tmp_home.path())
        .output()
        .expect("failed to run craterun");

    assert!(output.status.success());

    // List containers.
    let ps_output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .arg("ps")
        .env("HOME", tmp_home.path())
        .output()
        .expect("failed to run craterun ps");

    let ps_stdout = String::from_utf8_lossy(&ps_output.stdout);
    assert!(
        ps_stdout.contains("stopped"),
        "ps should show stopped container, got:\n{ps_stdout}"
    );
}

#[test]
fn smoke_rm_removes_container() {
    if !can_run() {
        eprintln!("Skipping integration test (prerequisites not met)");
        return;
    }

    let rootfs = rootfs_path();
    let tmp_home = tempfile::tempdir().unwrap();

    // Run a container.
    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args(["run", "--rootfs", &rootfs, "--", "/bin/true"])
        .env("HOME", tmp_home.path())
        .output()
        .expect("failed to run craterun");

    let container_id = String::from_utf8_lossy(&output.stdout)
        .lines()
        .next()
        .unwrap_or("")
        .trim()
        .to_string();

    // Remove it.
    let rm_output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args(["rm", &container_id])
        .env("HOME", tmp_home.path())
        .output()
        .expect("failed to run craterun rm");

    assert!(rm_output.status.success(), "rm should succeed");

    // ps should show nothing now.
    let ps_output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .arg("ps")
        .env("HOME", tmp_home.path())
        .output()
        .expect("failed to run craterun ps");

    let ps_stdout = String::from_utf8_lossy(&ps_output.stdout);
    // Should only have the header line.
    let lines: Vec<&str> = ps_stdout.lines().collect();
    assert_eq!(
        lines.len(),
        1,
        "ps should only show header after rm, got:\n{ps_stdout}"
    );
}

#[test]
fn smoke_memory_limit() {
    if !can_run() {
        eprintln!("Skipping integration test (prerequisites not met)");
        return;
    }

    let rootfs = rootfs_path();
    let tmp_home = tempfile::tempdir().unwrap();

    // Run with a memory limit — just verify it doesn't crash.
    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args([
            "run",
            "--rootfs",
            &rootfs,
            "--memory",
            "67108864",
            "--",
            "/bin/sh",
            "-c",
            "echo mem_ok",
        ])
        .env("HOME", tmp_home.path())
        .output()
        .expect("failed to run craterun with memory limit");

    assert!(
        output.status.success(),
        "should succeed with memory limit, stderr: {}",
        String::from_utf8_lossy(&output.stderr)
    );
}

#[test]
fn smoke_refuses_root_as_rootfs() {
    if !can_run() {
        eprintln!("Skipping integration test (prerequisites not met)");
        return;
    }

    let tmp_home = tempfile::tempdir().unwrap();

    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args(["run", "--rootfs", "/", "--", "/bin/true"])
        .env("HOME", tmp_home.path())
        .output()
        .expect("failed to run craterun");

    assert!(
        !output.status.success(),
        "should refuse / as rootfs"
    );
    let stderr = String::from_utf8_lossy(&output.stderr);
    assert!(
        stderr.contains("refusing") || stderr.contains("destroy"),
        "error message should warn about using / as rootfs, got: {stderr}"
    );
}

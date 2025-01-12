/// Tests for CLI argument parsing and configuration validation.

use std::process::Command;

/// Verify the binary can print help without error.
#[test]
fn cli_help_works() {
    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .arg("--help")
        .output()
        .expect("failed to execute craterun --help");

    let stdout = String::from_utf8_lossy(&output.stdout);
    assert!(
        stdout.contains("craterun") || stdout.contains("CrateRun"),
        "help output should mention craterun"
    );
}

/// Verify `run` requires --rootfs and a command.
#[test]
fn cli_run_requires_rootfs() {
    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args(["run", "--", "/bin/sh"])
        .output()
        .expect("failed to execute craterun run");

    assert!(
        !output.status.success(),
        "run without --rootfs should fail"
    );
}

/// Verify `run` requires at least one command argument.
#[test]
fn cli_run_requires_cmd() {
    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args(["run", "--rootfs", "/nonexistent"])
        .output()
        .expect("failed to execute craterun run");

    assert!(
        !output.status.success(),
        "run without command should fail"
    );
}

/// Verify `ps` succeeds even with no containers.
#[test]
fn cli_ps_empty() {
    let tmp = tempfile::tempdir().unwrap();
    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .arg("ps")
        .env("HOME", tmp.path())
        .output()
        .expect("failed to execute craterun ps");

    let stdout = String::from_utf8_lossy(&output.stdout);
    assert!(
        stdout.contains("CONTAINER ID"),
        "ps should print a header"
    );
}

/// Verify `rm` with a non-existent ID fails gracefully.
#[test]
fn cli_rm_nonexistent() {
    let tmp = tempfile::tempdir().unwrap();
    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args(["rm", "deadbeef"])
        .env("HOME", tmp.path())
        .output()
        .expect("failed to execute craterun rm");

    assert!(!output.status.success());
    let stderr = String::from_utf8_lossy(&output.stderr);
    assert!(
        stderr.contains("no container found"),
        "should report no container found, got: {stderr}"
    );
}

/// Verify `logs` with a non-existent ID fails gracefully.
#[test]
fn cli_logs_nonexistent() {
    let tmp = tempfile::tempdir().unwrap();
    let output = Command::new(env!("CARGO_BIN_EXE_craterun"))
        .args(["logs", "deadbeef"])
        .env("HOME", tmp.path())
        .output()
        .expect("failed to execute craterun logs");

    assert!(!output.status.success());
    let stderr = String::from_utf8_lossy(&output.stderr);
    assert!(
        stderr.contains("no container found"),
        "should report no container found, got: {stderr}"
    );
}

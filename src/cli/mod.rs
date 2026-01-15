pub mod commands;

use clap::{Parser, Subcommand};

/// CrateRun — a minimal Linux container runtime.
#[derive(Parser, Debug)]
#[command(name = "craterun", version, about)]
pub struct Cli {
    #[command(subcommand)]
    pub command: Command,
}

#[derive(Subcommand, Debug)]
pub enum Command {
    /// Create and run a new container.
    Run {
        /// Path to the root filesystem (e.g. an extracted Alpine minirootfs).
        #[arg(long)]
        rootfs: String,

        /// Memory limit in bytes (e.g. 67108864 for 64 MiB). Passed to cgroup memory.max.
        #[arg(long)]
        memory: Option<u64>,

        /// CPU bandwidth in the form `quota period` (microseconds), e.g. "100000 100000" for 100 %.
        /// Passed to cgroup cpu.max.
        #[arg(long)]
        cpu: Option<String>,

        /// Maximum number of PIDs in the container.
        #[arg(long)]
        pids: Option<u64>,

        /// UID to map inside the container (host UID that becomes root inside). Optional.
        #[arg(long)]
        uid: Option<u32>,

        /// GID to map inside the container. Optional.
        #[arg(long)]
        gid: Option<u32>,

        /// Hostname to set inside the container (default: "craterun").
        #[arg(long, default_value = "craterun")]
        hostname: String,

        /// The command (and arguments) to execute inside the container.
        /// Everything after `--` is treated as the command.
        #[arg(last = true, required = true)]
        cmd: Vec<String>,
    },

    /// List containers.
    Ps,

    /// Remove a stopped container.
    Rm {
        /// Container ID (or unique prefix).
        id: String,

        /// Force-remove even if the container is still running.
        #[arg(long)]
        force: bool,
    },

    /// Print the stdout/stderr logs of a container.
    Logs {
        /// Container ID (or unique prefix).
        id: String,
    },

    /// Execute a command inside a running container.
    Exec {
        /// Container ID (or unique prefix).
        id: String,

        /// The command (and arguments) to execute.
        #[arg(last = true, required = true)]
        cmd: Vec<String>,
    },
}

/// Parse CLI arguments. Called from `main`.
pub fn parse() -> Cli {
    Cli::parse()
}

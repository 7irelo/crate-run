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
        /// Optional human-readable name. Must be unique, and may be used in
        /// place of the container ID with ps, logs, exec, inspect and rm.
        #[arg(long)]
        name: Option<String>,

        /// Path to the root filesystem (e.g. an extracted Alpine minirootfs).
        #[arg(long)]
        rootfs: String,

        /// Memory limit, e.g. 256m, 2g, 64k, or a plain byte count.
        /// Suffixes are binary multiples. Passed to cgroup memory.max.
        #[arg(long)]
        memory: Option<String>,

        /// Number of CPUs, e.g. 1.0 or 0.5. Converted to a cgroup cpu.max
        /// quota against the default 100000us period. Conflicts with --cpu.
        #[arg(long, conflicts_with = "cpu")]
        cpus: Option<f64>,

        /// Raw cgroup cpu.max value in the form `quota period` (microseconds),
        /// e.g. "100000 100000" for 100 %. Prefer --cpus.
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
        /// Container name, ID, or unique ID prefix.
        id: String,

        /// Force-remove even if the container is still running.
        #[arg(long)]
        force: bool,
    },

    /// Print the stdout/stderr logs of a container.
    Logs {
        /// Container name, ID, or unique ID prefix.
        id: String,

        /// Keep printing new output until interrupted.
        #[arg(short, long)]
        follow: bool,
    },

    /// Display detailed container metadata as JSON.
    Inspect {
        /// Container name, ID, or unique ID prefix.
        id: String,
    },

    /// Execute a command inside a running container.
    Exec {
        /// Container name, ID, or unique ID prefix.
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

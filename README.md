# CrateRun

A minimal Linux container runtime written in Rust. Think of it as a Docker-lite
for educational purposes — it uses real Linux kernel primitives (namespaces,
cgroups v2, pivot_root) to isolate and resource-limit processes.

> **Warning:** This is an educational project. It is **not** production-hardened.
> Do not use it to run untrusted workloads.

## Features (v1)

| Feature | Status |
|---|---|
| PID, mount, UTS, IPC, network namespaces | Done |
| `pivot_root` into a rootfs | Done |
| `/proc` and minimal `/dev` inside container | Done |
| cgroups v2: memory, CPU, PID limits | Done |
| Container state persistence (`ps`, `rm`, `logs`) | Done |
| stdout/stderr capture to log files | Done |
| `exec` into a running container | Done |
| Hostname isolation | Done |
| Exit-code propagation | Done |
| Rootfs safety validation | Done |

## Prerequisites

- **Linux** (x86_64). Tested on Ubuntu 22.04+.
- **Rust** stable toolchain (edition 2021).
- **Root privileges** — the runtime uses `unshare(2)`, `pivot_root(2)`, and
  writes to `/sys/fs/cgroup`, all of which require root or `CAP_SYS_ADMIN`.
- **cgroups v2** (unified hierarchy) mounted at `/sys/fs/cgroup`. Most modern
  distros (Ubuntu 22.04+, Fedora 31+) use this by default. Check with:

  ```bash
  mount | grep cgroup2
  # Expected: cgroup2 on /sys/fs/cgroup type cgroup2 (rw,...)
  ```

  If your system still uses cgroups v1, you can boot with
  `systemd.unified_cgroup_hierarchy=1` on the kernel command line.

## Getting a Rootfs

CrateRun needs an extracted root filesystem. The easiest option is Alpine
Linux's minirootfs:

```bash
mkdir -p /tmp/alpine-rootfs
curl -fsSL https://dl-cdn.alpinelinux.org/alpine/v3.20/releases/x86_64/alpine-minirootfs-3.20.3-x86_64.tar.gz \
    | tar -xz -C /tmp/alpine-rootfs
```

You can also use `debootstrap` for Debian/Ubuntu or extract any OCI image layer.

## Building

```bash
cd craterun
cargo build --release
```

The binary is at `target/release/craterun`.

## Usage

### Run a container

```bash
sudo ./target/release/craterun run \
    --rootfs /tmp/alpine-rootfs \
    -- /bin/sh -c 'echo "Hello from container!"'
```

This prints the container ID to stdout and exits with the container's exit code.

### Run with resource limits

```bash
sudo ./target/release/craterun run \
    --rootfs /tmp/alpine-rootfs \
    --memory 67108864 \
    --pids 50 \
    --cpu "50000 100000" \
    --hostname mycontainer \
    -- /bin/sh -c 'echo "limited!"'
```

- `--memory 67108864` — 64 MiB memory limit
- `--pids 50` — max 50 processes
- `--cpu "50000 100000"` — 50% of one CPU (50ms quota per 100ms period)
- `--hostname mycontainer` — UTS hostname inside the container

### List containers

```bash
sudo ./target/release/craterun ps
```

Output:

```
CONTAINER ID       PID      STATUS     CREATED                  COMMAND
a1b2c3d4e5f67890   -        stopped    2025-06-15 10:30:00 UTC  /bin/sh -c echo Hello...
```

### View logs

```bash
sudo ./target/release/craterun logs a1b2c3d4
```

Prints the stdout (and stderr to stderr) captured during the container's run.

### Remove a container

```bash
sudo ./target/release/craterun rm a1b2c3d4
```

Use `--force` to remove a running container (sends SIGKILL first):

```bash
sudo ./target/release/craterun rm --force a1b2c3d4
```

### Exec into a running container

```bash
sudo ./target/release/craterun exec a1b2c3d4 -- /bin/sh
```

This enters the namespaces of the running container and executes the given
command. Useful for debugging.

## Architecture

```
src/
├── main.rs              Entry point
├── cli/
│   ├── mod.rs           Argument definitions (clap derive)
│   └── commands.rs      Command dispatch and handlers
├── core/
│   ├── mod.rs
│   ├── id.rs            Container ID generation
│   ├── model.rs         Data models (ContainerMeta, ContainerConfig, etc.)
│   └── state.rs         State persistence (save/load/list/resolve)
├── platform/
│   ├── mod.rs
│   └── linux/
│       ├── mod.rs
│       ├── namespaces.rs   unshare, clone flags, sethostname
│       ├── mounts.rs       bind mount, pivot_root, mount /proc and /dev
│       ├── cgroups.rs      cgroups v2 setup and teardown
│       └── process.rs      fork, exec, container lifecycle
└── util/
    ├── mod.rs
    └── fs.rs            Filesystem helpers
```

**Separation of concerns:**

- `core/` — Pure logic: models, validation, state persistence. No syscalls.
- `platform/linux/` — All Linux-specific syscalls and kernel interactions.
- `cli/` — Argument parsing and user-facing output. Thin layer over core + platform.
- `util/` — Shared filesystem helpers.

## Testing

### Unit tests

```bash
cargo test --lib
cargo test --test unit_id --test unit_state --test unit_config
```

These run anywhere (including macOS/Windows for compilation checks).

### Integration tests

Integration tests require Linux + root + an Alpine rootfs:

```bash
# Prepare rootfs
mkdir -p tests/rootfs
curl -fsSL https://dl-cdn.alpinelinux.org/alpine/v3.20/releases/x86_64/alpine-minirootfs-3.20.3-x86_64.tar.gz \
    | tar -xz -C tests/rootfs

# Run as root
sudo env "PATH=$PATH" CRATERUN_TEST_ROOTFS=tests/rootfs cargo test --test integration_smoke -- --test-threads=1
```

Integration tests automatically skip if not running as root or if the rootfs is
missing.

## CI

GitHub Actions runs on every pull request and push to main/master:

1. **Lint & Unit Tests** — `cargo fmt --check`, `cargo clippy`, unit tests.
2. **Integration Tests** — Downloads Alpine rootfs, runs smoke tests as root.

See [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

## Container State

State is stored in:

- `/var/lib/craterun/<id>/` when running as root
- `~/.craterun/<id>/` when running as a regular user

Each container directory contains:

- `metadata.json` — container metadata (ID, rootfs, cmd, PID, status, timestamps, limits)
- `stdout.log` — captured stdout
- `stderr.log` — captured stderr

## Limitations (v1)

- **Network namespace** is created but no veth pair or bridge is configured.
  The container gets an isolated, empty network stack (loopback only). Use
  `--net=host` in a future version if you need host networking.
- **User namespaces** are not used in v1. The runtime requires root.
- **Seccomp** filters are not applied. The container can make any syscall.
- **Capabilities** are not explicitly dropped beyond what namespaces provide.
- **Storage** — no overlay filesystem or copy-on-write. The rootfs is used
  directly (consider using a read-only bind mount in production).
- **No image pulling** — you must provide a pre-extracted rootfs.
- **Single-host only** — no networking, orchestration, or registry support.

## Security Notes

- This is an **educational project**. The isolation it provides is real (kernel
  namespaces + cgroups) but incomplete for production use.
- The runtime refuses to use `/` as a rootfs to prevent host destruction.
- The rootfs is validated to contain at least `bin/`, `usr/`, or `etc/`.
- No seccomp or AppArmor profiles are applied.
- The container runs as root inside its namespaces. In a production runtime you
  would map UIDs via user namespaces and drop capabilities.

## License

MIT

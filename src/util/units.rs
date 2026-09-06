//! Parsing of human-friendly resource limit values.
//!
//! Container runtimes conventionally accept sizes like `256m` and CPU counts
//! like `1.5`, rather than raw bytes and cgroup `cpu.max` quota strings. These
//! helpers translate the friendly forms into the values the cgroup files want.

use anyhow::{bail, Result};

/// The cgroup v2 `cpu.max` period, in microseconds. This is the kernel default
/// and the denominator every quota below is expressed against.
pub const CPU_PERIOD_US: u64 = 100_000;

/// Parse a human-readable byte size such as `512`, `64k`, `256m`, or `2g`.
///
/// A bare number is bytes. Suffixes are binary multiples (1k = 1024), matching
/// how Docker interprets them. Both `256m` and `256M` are accepted, as are the
/// longer `256mb` / `256MiB` spellings.
pub fn parse_size(input: &str) -> Result<u64> {
    let trimmed = input.trim();
    if trimmed.is_empty() {
        bail!("memory limit cannot be empty");
    }

    let lowered = trimmed.to_ascii_lowercase();
    // Strip an optional trailing "b", "ib" or "б"-free unit tail so that
    // "256m", "256mb" and "256mib" all reduce to a number plus one letter.
    let stripped = lowered
        .strip_suffix("ib")
        .or_else(|| lowered.strip_suffix('b'))
        .unwrap_or(&lowered);

    let (digits, multiplier) = match stripped.chars().last() {
        Some('k') => (&stripped[..stripped.len() - 1], 1024u64),
        Some('m') => (&stripped[..stripped.len() - 1], 1024u64 * 1024),
        Some('g') => (&stripped[..stripped.len() - 1], 1024u64 * 1024 * 1024),
        Some('t') => (&stripped[..stripped.len() - 1], 1024u64 * 1024 * 1024 * 1024),
        // No unit letter: the value is already in bytes.
        Some(c) if c.is_ascii_digit() => (stripped, 1u64),
        _ => bail!("invalid memory limit '{input}': expected a size like 512, 64k, 256m or 2g"),
    };

    if digits.is_empty() {
        bail!("invalid memory limit '{input}': missing a number before the unit");
    }

    let value: u64 = digits
        .parse()
        .map_err(|_| anyhow::anyhow!("invalid memory limit '{input}': '{digits}' is not a number"))?;

    value
        .checked_mul(multiplier)
        .ok_or_else(|| anyhow::anyhow!("memory limit '{input}' is too large"))
}

/// Convert a CPU count such as `1.0` or `0.5` into a cgroup v2 `cpu.max` value.
///
/// `1.0` means one full core and becomes `"100000 100000"`; `0.5` becomes
/// `"50000 100000"`. Values above the host's core count are allowed — the
/// kernel simply never grants more than is available.
pub fn cpus_to_cpu_max(cpus: f64) -> Result<String> {
    if !cpus.is_finite() || cpus <= 0.0 {
        bail!("invalid --cpus value '{cpus}': must be a positive number");
    }

    let quota = (cpus * CPU_PERIOD_US as f64).round() as u64;
    if quota == 0 {
        bail!("invalid --cpus value '{cpus}': too small to express as a CPU quota");
    }

    Ok(format!("{quota} {CPU_PERIOD_US}"))
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_bare_bytes() {
        assert_eq!(parse_size("512").unwrap(), 512);
        assert_eq!(parse_size("67108864").unwrap(), 67_108_864);
    }

    #[test]
    fn parses_binary_suffixes() {
        assert_eq!(parse_size("1k").unwrap(), 1024);
        assert_eq!(parse_size("256m").unwrap(), 256 * 1024 * 1024);
        assert_eq!(parse_size("2g").unwrap(), 2 * 1024 * 1024 * 1024);
    }

    #[test]
    fn suffix_spelling_and_case_do_not_matter() {
        let expected = 256 * 1024 * 1024;
        assert_eq!(parse_size("256M").unwrap(), expected);
        assert_eq!(parse_size("256mb").unwrap(), expected);
        assert_eq!(parse_size("256MiB").unwrap(), expected);
        assert_eq!(parse_size(" 256m ").unwrap(), expected);
    }

    #[test]
    fn rejects_nonsense_sizes() {
        assert!(parse_size("").is_err());
        assert!(parse_size("m").is_err());
        assert!(parse_size("abc").is_err());
        assert!(parse_size("1.5g").is_err());
    }

    #[test]
    fn converts_cpu_counts() {
        assert_eq!(cpus_to_cpu_max(1.0).unwrap(), "100000 100000");
        assert_eq!(cpus_to_cpu_max(0.5).unwrap(), "50000 100000");
        assert_eq!(cpus_to_cpu_max(2.0).unwrap(), "200000 100000");
    }

    #[test]
    fn rejects_nonsense_cpu_counts() {
        assert!(cpus_to_cpu_max(0.0).is_err());
        assert!(cpus_to_cpu_max(-1.0).is_err());
        assert!(cpus_to_cpu_max(f64::NAN).is_err());
    }
}

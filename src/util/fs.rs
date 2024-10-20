use std::fs;
use std::path::Path;

use anyhow::{Context, Result};

/// Recursively ensure a directory exists.
pub fn ensure_dir(path: &Path) -> Result<()> {
    fs::create_dir_all(path)
        .with_context(|| format!("failed to create directory {}", path.display()))
}

/// Read a file to string, returning a descriptive error on failure.
pub fn read_to_string(path: &Path) -> Result<String> {
    fs::read_to_string(path)
        .with_context(|| format!("failed to read {}", path.display()))
}

/// Write contents to a file, creating parent directories if needed.
pub fn write_file(path: &Path, contents: &str) -> Result<()> {
    if let Some(parent) = path.parent() {
        ensure_dir(parent)?;
    }
    fs::write(path, contents)
        .with_context(|| format!("failed to write {}", path.display()))
}

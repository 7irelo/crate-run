use rand::Rng;

/// Length of a container ID in hex characters.
const ID_LEN: usize = 16;

/// Generate a random hex container ID (16 hex chars = 8 random bytes).
pub fn generate_id() -> String {
    let mut rng = rand::thread_rng();
    let bytes: Vec<u8> = (0..ID_LEN / 2).map(|_| rng.gen()).collect();
    hex_encode(&bytes)
}

/// Encode bytes as a lowercase hex string.
fn hex_encode(bytes: &[u8]) -> String {
    let mut s = String::with_capacity(bytes.len() * 2);
    for b in bytes {
        s.push_str(&format!("{b:02x}"));
    }
    s
}

/// Validate that a string looks like a valid container-ID prefix.
/// Must be non-empty, lowercase hex, and at most `ID_LEN` characters.
pub fn validate_id_prefix(prefix: &str) -> bool {
    !prefix.is_empty()
        && prefix.len() <= ID_LEN
        && prefix.chars().all(|c| c.is_ascii_hexdigit() && !c.is_ascii_uppercase())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn generated_id_has_correct_length() {
        let id = generate_id();
        assert_eq!(id.len(), ID_LEN);
    }

    #[test]
    fn generated_id_is_hex() {
        let id = generate_id();
        assert!(id.chars().all(|c| c.is_ascii_hexdigit()));
    }

    #[test]
    fn generated_id_is_lowercase() {
        let id = generate_id();
        assert_eq!(id, id.to_lowercase());
    }

    #[test]
    fn validate_prefix_accepts_valid() {
        assert!(validate_id_prefix("ab12"));
        assert!(validate_id_prefix("0123456789abcdef"));
    }

    #[test]
    fn validate_prefix_rejects_invalid() {
        assert!(!validate_id_prefix(""));
        assert!(!validate_id_prefix("ABCD")); // uppercase
        assert!(!validate_id_prefix("0123456789abcdef0")); // too long
        assert!(!validate_id_prefix("zzzz")); // non-hex
    }
}

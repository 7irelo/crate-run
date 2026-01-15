mod cli;
mod core;
mod platform;
mod util;

use std::process;

fn main() {
    let args = cli::parse();

    if let Err(e) = cli::commands::dispatch(args) {
        eprintln!("craterun: {e:#}");
        process::exit(1);
    }
}

#!/usr/bin/env python3
"""Run lightweight checks before publishing the Soda source tree."""

from __future__ import annotations

import argparse
import ipaddress
import os
import re
import sys
from pathlib import Path


IGNORED_DIRECTORIES = {
    ".git",
}
FORBIDDEN_PUBLISH_DIRECTORIES = {
    ".cache",
    ".idea",
    ".pytest_cache",
    ".vscode",
    "__pycache__",
    "coverage",
    "dist",
    "logs",
    "node_modules",
    "runtime-logs",
    "target",
}
TEXT_SUFFIXES = {
    ".css",
    ".html",
    ".http",
    ".java",
    ".js",
    ".json",
    ".md",
    ".properties",
    ".py",
    ".sql",
    ".svg",
    ".xml",
    ".yaml",
    ".yml",
}
FORBIDDEN_FILE_SUFFIXES = {
    ".bak",
    ".class",
    ".ear",
    ".jar",
    ".jks",
    ".key",
    ".log",
    ".p12",
    ".pem",
    ".pfx",
    ".pid",
    ".pyc",
    ".pyo",
    ".tmp",
    ".war",
    ".zip",
}
DEFAULT_FORBIDDEN_TERMS = (
    "5" + "8" + ".com",
    "5" + "8" + "同城",
    "五" + "八" + "同城",
    "五" + "八",
    "wu" + "ba",
    "5" + "8" + "corp",
    "5" + "8" + "ganji",
    "S" + "CF",
)
IPV4_PATTERN = re.compile(r"(?<![\d.])(?:\d{1,3}\.){3}\d{1,3}(?![\d.])")
EMAIL_PATTERN = re.compile(r"[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}", re.IGNORECASE)
DOCUMENTATION_NETWORKS = tuple(
    ipaddress.ip_network(value)
    for value in ("192.0.2.0/24", "198.51.100.0/24", "203.0.113.0/24")
)


def arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "root",
        nargs="?",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help="repository root (defaults to the parent of tools)",
    )
    parser.add_argument(
        "--forbidden-term",
        action="append",
        default=list(DEFAULT_FORBIDDEN_TERMS),
        help=(
            "case-insensitive organization or internal-system term to reject; repeatable; "
            "Soda's historical-brand denylist is enabled by default"
        ),
    )
    return parser.parse_args()


def source_files(root: Path):
    for current, directories, files in os.walk(root):
        directories[:] = [
            name
            for name in directories
            if name not in IGNORED_DIRECTORIES
            and name not in FORBIDDEN_PUBLISH_DIRECTORIES
        ]
        current_path = Path(current)
        for name in files:
            yield current_path / name


def forbidden_publish_directories(root: Path):
    for current, directories, _ in os.walk(root):
        current_path = Path(current)
        retained: list[str] = []
        for name in directories:
            if name in IGNORED_DIRECTORIES:
                continue
            path = current_path / name
            if name in FORBIDDEN_PUBLISH_DIRECTORIES:
                yield path
                continue
            retained.append(name)
        directories[:] = retained


def is_private_ip(value: str) -> bool:
    try:
        address = ipaddress.ip_address(value)
    except ValueError:
        return False
    if any(address in network for network in DOCUMENTATION_NETWORKS):
        return False
    return address.is_private and not address.is_loopback


def main() -> int:
    args = arguments()
    root = args.root.resolve()
    if not root.is_dir():
        print(f"error: repository root does not exist: {root}", file=sys.stderr)
        return 2

    findings: list[str] = []
    terms = [term.casefold() for term in args.forbidden_term if term.strip()]

    for path in forbidden_publish_directories(root):
        findings.append(f"{path.relative_to(root)}: generated or local-only directory")

    for path in source_files(root):
        relative = path.relative_to(root)
        if path.suffix.lower() in FORBIDDEN_FILE_SUFFIXES:
            findings.append(f"{relative}: forbidden publish-time file type")
            continue
        if path.suffix.lower() not in TEXT_SUFFIXES and path.name not in {
            ".editorconfig",
            ".gitattributes",
            ".gitignore",
            "Dockerfile",
        }:
            continue
        try:
            text = path.read_text(encoding="utf-8")
        except (UnicodeDecodeError, OSError) as error:
            findings.append(f"{relative}: cannot read as UTF-8 ({error})")
            continue

        folded = text.casefold()
        for term in terms:
            if term in folded:
                findings.append(f"{relative}: contains forbidden term {term!r}")

        for line_number, line in enumerate(text.splitlines(), start=1):
            for candidate in IPV4_PATTERN.findall(line):
                if is_private_ip(candidate):
                    findings.append(f"{relative}:{line_number}: private IPv4 address {candidate}")
            for email in EMAIL_PATTERN.findall(line):
                if not email.lower().endswith("@example.invalid"):
                    findings.append(f"{relative}:{line_number}: email address {email}")

    if findings:
        print("Open-source check failed:")
        for finding in findings:
            print(f"- {finding}")
        return 1

    print(f"Open-source check passed: {root}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

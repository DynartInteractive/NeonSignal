import os
import re

ROOT_DIR = "."  # e.g. "./src/main/java"

REPLACEMENTS = [
    # package
    (re.compile(r"\binfo\.dynart\.coolfox\b"), "net.dynart.neonsignal"),

    # CoolFox* → NeonSignal*
    (re.compile(r"\bCoolFox(?=[A-Z_])"), "NeonSignal"),

    # lowercase identifiers
    (re.compile(r"\bcoolfox(?=[a-z_])"), "neonsignal"),
]

def process_java_files(root_dir: str) -> None:
    for root, _, files in os.walk(root_dir):
        for filename in files:
            if not filename.endswith(".java"):
                continue

            path = os.path.join(root, filename)

            with open(path, "r", encoding="utf-8") as f:
                original = f.read()

            updated = original
            changed = False

            for pattern, replacement in REPLACEMENTS:
                updated, count = pattern.subn(replacement, updated)
                if count > 0:
                    changed = True

            if changed:
                with open(path, "w", encoding="utf-8") as f:
                    f.write(updated)
                print(f"Updated: {path}")

if __name__ == "__main__":
    process_java_files(ROOT_DIR)
    print("Done.")

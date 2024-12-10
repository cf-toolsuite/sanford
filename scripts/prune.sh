#!/usr/bin/env bash

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Get the parent (root) directory
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

# Change to the root directory
cd "$ROOT_DIR" || exit 1

export BACKUP_LOC=/tmp/sanford/$(date +"%Y.%m.%d.%N" | cut -b1-14)
mkdir -p "$BACKUP_LOC"

# Candidates to be pruned
candidates=(".history" ".github" ".gradle" ".trunk" "config" "docs" "docker" "scripts" "bin" "build" "data" "samples" "README.md")

echo "Running from directory: $(pwd)"
echo "The following items will be pruned:"

# First, list what will be deleted
for item in "${candidates[@]}"; do
    if [ -e "$item" ] || [ -d "$item" ] || compgen -G "$item" > /dev/null; then
        echo "- $item"
    fi
done

# Ask for confirmation
read -p "Are you sure you want to proceed? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Operation cancelled."
    exit 1
fi

# Proceed with backup and deletion
for item in "${candidates[@]}"; do
    if [ -e "$item" ] || [ -d "$item" ] || compgen -G "$item" > /dev/null; then
        echo "Backing up and removing: $item"
        cp -Rf "$item" "$BACKUP_LOC/"
        rm -Rf "$item"
    fi
done

echo "Backup created at: $BACKUP_LOC"

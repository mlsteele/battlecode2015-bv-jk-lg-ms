#!/usr/bin/env bash
cd $(dirname $0)

function rebuild () {
    python rebuild_headers.py "$1";
    echo "rebuilt $1"
}

shopt -s globstar
for i in team017/**/*.java; do
    rebuild "$i"
done

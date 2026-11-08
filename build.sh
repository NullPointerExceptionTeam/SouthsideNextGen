#!/bin/sh
set -eu

project=$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd)
gradle_bin=${GRADLE_BIN:-gradle}
if ! command -v "$gradle_bin" >/dev/null 2>&1; then
    printf '%s\n' 'Gradle 8.14 is required. Put it on PATH or set GRADLE_BIN.' >&2
    exit 1
fi

export GRADLE_USER_HOME=${GRADLE_USER_HOME:-"$project/.build-local/gradle"}
export TMPDIR="$project/.build-local/tmp"
mkdir -p "$GRADLE_USER_HOME" "$TMPDIR"

exec "$gradle_bin" --no-daemon --project-dir "$project" "-Djava.io.tmpdir=$TMPDIR" clean build "$@"

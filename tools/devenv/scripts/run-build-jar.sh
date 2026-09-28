#!/bin/sh
# Starts the newest shaded jar (*-all.jar) in a Gradle build output folder: <folder> [jvm args...].
set -eu
DIR="$1"
shift
JAR=$(ls -t "$DIR"/*-all.jar 2>/dev/null | head -n 1 || true)
if [ -z "$JAR" ]; then
    echo "No *-all.jar in $DIR. Build it first, for example with ./gradlew build." >&2
    exit 1
fi
echo "Starting $JAR"
exec java "$@" -jar "$JAR"

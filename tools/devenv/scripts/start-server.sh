#!/bin/sh
# Prepares and starts a Paper or Velocity server in /data: <paper|velocity> [server args...].
#
# Template files are copied into /data only where no file exists yet, so local edits survive.
# The plugin jars are replaced on every start: the fetched plugins for this server kind plus the
# newest roleplay *-all.jar from the Gradle build output mounted at /build.
set -eu

KIND="$1"
shift

mkdir -p /data/plugins
cp -r --update=none /templates/. /data/

find /data/plugins -maxdepth 1 -name '*.jar' -delete
cp /jars/"$KIND"-plugins/*.jar /data/plugins/

ROLEPLAY=$(ls -t /build/*-all.jar 2>/dev/null | head -n 1 || true)
if [ -z "$ROLEPLAY" ]; then
    echo "No roleplay *-all.jar in /build. Build it first, for example with ./gradlew build." >&2
    exit 1
fi
cp "$ROLEPLAY" /data/plugins/
echo "Installed $(basename "$ROLEPLAY")"

cd /data
exec java ${JVM_ARGS:-} -jar /jars/server/"$KIND".jar "$@"

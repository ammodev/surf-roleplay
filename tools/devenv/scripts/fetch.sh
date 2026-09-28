#!/bin/sh
# Downloads every server, plugin and microservice jar pinned in versions.env into /jars.
# Does nothing when /jars already holds the jars for the current versions.env.
set -eu

JARS=/jars
STAMP="$JARS/.versions"

if [ -f "$STAMP" ] && cmp -s "$STAMP" /versions.env; then
    echo "Jars are up to date."
    exit 0
fi

rm -rf "$JARS/server" "$JARS/paper-plugins" "$JARS/velocity-plugins" "$JARS/microservices" "$STAMP"
mkdir -p "$JARS/server" "$JARS/paper-plugins" "$JARS/velocity-plugins" "$JARS/microservices"

# Downloads a URL to a file.
get() {
    echo "Fetching $2"
    curl -fsSL --retry 3 -o "$2" "$1"
}

# Downloads a release asset of an SLNE-Development repository: <repo> <version> <asset> <file>.
release() {
    get "https://github.com/SLNE-Development/$1/releases/download/v$2/$3" "$4"
}

get "$PAPER_URL" "$JARS/server/paper.jar"
get "$VELOCITY_URL" "$JARS/server/velocity.jar"

P="$JARS/paper-plugins"
release surf-api "$SURF_API_VERSION" "surf-api-paper-server-$SURF_API_VERSION-all.jar" "$P/surf-api-paper.jar"
release surf-core "$SURF_CORE_VERSION" "surf-core-paper-$SURF_CORE_VERSION-all.jar" "$P/surf-core-paper.jar"
release surf-redis "$SURF_REDIS_VERSION" "surf-redis-paper-$SURF_REDIS_VERSION-all.jar" "$P/surf-redis-paper.jar"
release surf-rabbitmq "$SURF_RABBITMQ_VERSION" "surf-rabbitmq-paper-$SURF_RABBITMQ_VERSION-all.jar" "$P/surf-rabbitmq-paper.jar"
release surf-transaction "$SURF_TRANSACTION_VERSION" "surf-transaction-paper-$SURF_TRANSACTION_VERSION-all.jar" "$P/surf-transaction-paper.jar"
get "$COMMANDAPI_PAPER_URL" "$P/commandapi.jar"
get "$LUCKPERMS_PAPER_URL" "$P/luckperms.jar"
get "$PACKETEVENTS_PAPER_URL" "$P/packetevents.jar"

V="$JARS/velocity-plugins"
release surf-api "$SURF_API_VERSION" "surf-api-velocity-server-$SURF_API_VERSION-all.jar" "$V/surf-api-velocity.jar"
release surf-core "$SURF_CORE_VERSION" "surf-core-velocity-$SURF_CORE_VERSION-all.jar" "$V/surf-core-velocity.jar"
release surf-redis "$SURF_REDIS_VERSION" "surf-redis-velocity-$SURF_REDIS_VERSION-all.jar" "$V/surf-redis-velocity.jar"
release surf-rabbitmq "$SURF_RABBITMQ_VERSION" "surf-rabbitmq-velocity-$SURF_RABBITMQ_VERSION-all.jar" "$V/surf-rabbitmq-velocity.jar"
release surf-transaction "$SURF_TRANSACTION_VERSION" "surf-transaction-velocity-$SURF_TRANSACTION_VERSION-all.jar" "$V/surf-transaction-velocity.jar"
get "$COMMANDAPI_VELOCITY_URL" "$V/commandapi.jar"
get "$LUCKPERMS_VELOCITY_URL" "$V/luckperms.jar"
get "$PACKETEVENTS_VELOCITY_URL" "$V/packetevents.jar"

M="$JARS/microservices"
release surf-core "$SURF_CORE_VERSION" "surf-core-microservice-$SURF_CORE_VERSION-all.jar" "$M/surf-core.jar"
release surf-transaction "$SURF_TRANSACTION_VERSION" "surf-transaction-microservice-$SURF_TRANSACTION_VERSION-all.jar" "$M/surf-transaction.jar"

cp /versions.env "$STAMP"
echo "All jars fetched."

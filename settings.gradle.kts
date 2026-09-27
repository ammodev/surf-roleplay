pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://reposilite.slne.dev/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.slne.surf.api.gradle.settings") version "+"
}

rootProject.name = "surf-roleplay"

// Api
include(":surf-roleplay-api:surf-roleplay-api-common")
include(":surf-roleplay-api:surf-roleplay-api-client:surf-roleplay-api-client-common")
include(":surf-roleplay-api:surf-roleplay-api-client:surf-roleplay-api-client-paper")
include(":surf-roleplay-api:surf-roleplay-api-client:surf-roleplay-api-client-velocity")

// Core
include(":surf-roleplay-core:surf-roleplay-core-common")
include(":surf-roleplay-core:surf-roleplay-core-client:surf-roleplay-core-client-common")
include(":surf-roleplay-core:surf-roleplay-core-client:surf-roleplay-core-client-paper")
include(":surf-roleplay-core:surf-roleplay-core-client:surf-roleplay-core-client-velocity")

// Protocol
include(":surf-roleplay-protocol")

// Runtime
include(":surf-roleplay-paper")
include(":surf-roleplay-velocity")
include(":surf-roleplay-microservice")
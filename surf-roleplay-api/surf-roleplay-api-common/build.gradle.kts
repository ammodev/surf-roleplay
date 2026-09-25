plugins {
    id("dev.slne.surf.api.gradle.core")
}

surfCoreApi {
    withCoreCommon()
    withSurfRedis()
}

dependencies {
    compileOnlyApi("dev.slne.surf.transaction:surf-transaction-api:4.1.7")

    testImplementation("dev.slne.surf.transaction:surf-transaction-api:4.1.7")
    testImplementation("it.unimi.dsi:fastutil:8.5.19")
    testImplementation("net.kyori:adventure-api:5.2.0")
}

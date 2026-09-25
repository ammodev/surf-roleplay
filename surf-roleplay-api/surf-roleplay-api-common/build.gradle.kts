plugins {
    id("dev.slne.surf.api.gradle.core")
}

surfCoreApi {
    withCoreCommon()
    withSurfRedis()
}

dependencies {
    compileOnlyApi("dev.slne.surf.transaction:surf-transaction-api:4.1.7")
}

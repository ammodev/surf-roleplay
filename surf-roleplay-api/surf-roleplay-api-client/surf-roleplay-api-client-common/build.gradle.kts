plugins {
    id("dev.slne.surf.api.gradle.core")
}

surfCoreApi {
    withCoreCommon()
    withSurfRedis()
}

dependencies {
    api(projects.surfRoleplayApi.surfRoleplayApiCommon)
}
dependencies {
    testImplementation("net.kyori:adventure-api:5.2.0")
}

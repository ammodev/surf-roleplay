plugins {
    id("dev.slne.surf.api.gradle.core")
}

surfCoreApi {
    withCoreCommon()
    withSurfRedis()
}

dependencies {
    api(projects.surfRoleplayCore.surfRoleplayCoreCommon)
    api(projects.surfRoleplayApi.surfRoleplayApiClient.surfRoleplayApiClientCommon)
    compileOnlyApi("dev.slne.surf.transaction:surf-transaction-api:4.1.7")
}
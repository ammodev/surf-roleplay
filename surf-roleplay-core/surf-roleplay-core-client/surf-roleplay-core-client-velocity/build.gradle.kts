plugins {
    id("dev.slne.surf.api.gradle.velocity")
}

surfVelocityApi {
    withCoreVelocity()
    withSurfRedis()
}

dependencies {
    api(projects.surfRoleplayCore.surfRoleplayCoreClient.surfRoleplayCoreClientCommon)
    api(projects.surfRoleplayApi.surfRoleplayApiClient.surfRoleplayApiClientVelocity)
}
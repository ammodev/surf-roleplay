plugins {
    id("dev.slne.surf.api.gradle.velocity")
    id("dev.slne.surf.microservice")
}

surfVelocityApi {
    withCoreVelocity()
    withSurfRedis()
}

surfMicroservice {
    withClientVelocityApi()
}

dependencies {
    api(projects.surfRoleplayCore.surfRoleplayCoreClient.surfRoleplayCoreClientCommon)
    api(projects.surfRoleplayApi.surfRoleplayApiClient.surfRoleplayApiClientVelocity)
}
plugins {
    id("dev.slne.surf.api.gradle.paper-raw")
    id("dev.slne.surf.microservice")
}

surfRawPaperApi {
    withSurfRedis()
}

surfMicroservice {
    withClientPaperApi()
}

dependencies {
    api(projects.surfRoleplayCore.surfRoleplayCoreClient.surfRoleplayCoreClientCommon)
    api(projects.surfRoleplayApi.surfRoleplayApiClient.surfRoleplayApiClientPaper)
}
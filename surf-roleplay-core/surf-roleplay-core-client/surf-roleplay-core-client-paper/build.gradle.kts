plugins {
    id("dev.slne.surf.api.gradle.paper-raw")
}

surfRawPaperApi {
    withSurfRedis()
}

dependencies {
    api(projects.surfRoleplayCore.surfRoleplayCoreClient.surfRoleplayCoreClientCommon)
    api(projects.surfRoleplayApi.surfRoleplayApiClient.surfRoleplayApiClientPaper)
}
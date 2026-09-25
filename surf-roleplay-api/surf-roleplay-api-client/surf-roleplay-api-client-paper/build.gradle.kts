plugins {
    id("dev.slne.surf.api.gradle.paper-raw")
}

surfRawPaperApi {
    withSurfRedis()
}

dependencies {
    api(projects.surfRoleplayApi.surfRoleplayApiClient.surfRoleplayApiClientCommon)
}
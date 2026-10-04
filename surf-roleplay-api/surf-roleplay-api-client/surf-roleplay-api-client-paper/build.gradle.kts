plugins {
    id("dev.slne.surf.api.gradle.paper-raw")
}

surfRawPaperApi {
    withSurfRedis()
}

dependencies {
    api(projects.surfRoleplayApi.surfRoleplayApiClient.surfRoleplayApiClientCommon)

    testImplementation(kotlin("test"))
    testRuntimeOnly("com.google.flogger:flogger:0.9")
    testRuntimeOnly("com.google.flogger:flogger-system-backend:0.9")
}

tasks.test {
    useJUnitPlatform()
}

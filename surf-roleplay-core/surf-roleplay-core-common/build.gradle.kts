import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.api.gradle.core")
    id("dev.slne.surf.microservice")
}

surfCoreApi {
    withCoreCommon()
    withSurfRedis()
}

surfMicroservice {
    withCommonApi()
    withRabbitModule(RabbitModule.COMMON_API)
}

dependencies {
    api(projects.surfRoleplayApi.surfRoleplayApiCommon)

    testImplementation("it.unimi.dsi:fastutil:8.5.19")
    testImplementation("net.kyori:adventure-api:5.2.0")
}
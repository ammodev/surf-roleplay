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
    withClientCommonApi()
    withRabbitModule(RabbitModule.CLIENT_API)
}

dependencies {
    api(projects.surfRoleplayCore.surfRoleplayCoreCommon)
    api(projects.surfRoleplayApi.surfRoleplayApiClient.surfRoleplayApiClientCommon)

    testImplementation("dev.slne.surf.transaction:surf-transaction-api:4.1.7")
    testImplementation("it.unimi.dsi:fastutil:8.5.19")
    testImplementation("net.kyori:adventure-api:5.2.0")
}
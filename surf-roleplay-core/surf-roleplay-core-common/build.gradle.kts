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
}
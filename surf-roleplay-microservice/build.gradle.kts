import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.api.gradle.standalone")
    id("dev.slne.surf.microservice")
}

surfStandaloneApi {
    withSurfDatabaseR2dbc("+", "dev.slne.surf.roleplay.libs")
}

dependencies {
    api(projects.surfRoleplayCore.surfRoleplayCoreCommon)
}

surfMicroservice {
    withMicroserviceApi()
    withRabbitModule(RabbitModule.SERVER_API, true)
}
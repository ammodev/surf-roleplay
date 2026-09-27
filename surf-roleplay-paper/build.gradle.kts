import dev.slne.surf.api.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.roleplay.paper.PaperMain")
    withCorePaper()
    withSurfRedis()

    serverDependencies {
        registerRequired("surf-transaction-paper")
    }
}

dependencies {
    api(projects.surfRoleplayCore.surfRoleplayCoreClient.surfRoleplayCoreClientPaper)
    implementation(projects.surfRoleplayProtocol)
}

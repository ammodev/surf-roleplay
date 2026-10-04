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

    testRuntimeOnly("com.google.flogger:flogger:0.9")
    testRuntimeOnly("com.google.flogger:flogger-system-backend:0.9")
}

evaluationDependsOn(":surf-roleplay-fabric")

/** The task of the Fabric module that generates the Lucide icon index. */
val rasterizeLucide = rootProject.project(":surf-roleplay-fabric").tasks.named("rasterizeLucide")

tasks.processResources {
    dependsOn(rasterizeLucide)
    from(rasterizeLucide.map { it.outputs.files.asFileTree.matching { include("**/surf-roleplay/lucide/index.json") } }) {
        into("lucide")
        eachFile { path = "lucide/index.json" }
        includeEmptyDirs = false
    }
}

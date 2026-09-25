plugins {
    id("dev.slne.surf.api.gradle.velocity")
}

surfVelocityApi {
    //mainClass("dev.slne.surf.roleplay.paper.VelocityMain")
    withCoreVelocity()
    withSurfRedis()
}

dependencies {
    api(projects.surfRoleplayCore.surfRoleplayCoreClient.surfRoleplayCoreClientVelocity)
}
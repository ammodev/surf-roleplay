plugins {
    id("dev.slne.surf.api.gradle.velocity")
}

surfVelocityApi {
    withCoreVelocity()
    withSurfRedis()
}

velocityPluginFile {
    main = "dev.slne.surf.roleplay.velocity.VelocityMain"
}

dependencies {
    api(projects.surfRoleplayCore.surfRoleplayCoreClient.surfRoleplayCoreClientVelocity)
}
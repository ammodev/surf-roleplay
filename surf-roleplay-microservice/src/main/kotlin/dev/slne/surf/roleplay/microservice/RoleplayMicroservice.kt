package dev.slne.surf.roleplay.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.microservice.api.microservice.Microservice
import kotlin.io.path.Path

@AutoService(Microservice::class)
class RoleplayMicroservice : Microservice() {
    override val dataPath = Path("config")

    override suspend fun onBootstrap(args: List<String>) {
        // Register handlers here.
    }
}
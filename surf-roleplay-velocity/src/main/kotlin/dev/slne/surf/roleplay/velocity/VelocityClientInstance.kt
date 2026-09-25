package dev.slne.surf.roleplay.velocity

import com.google.auto.service.AutoService
import dev.slne.surf.roleplay.core.client.common.ClientInstance
import java.nio.file.Path

/**
 * Velocity client instance implementation.
 *
 * Provides Velocity-specific data path resolution for the client instance.
 */
@AutoService(ClientInstance::class)
class VelocityClientInstance : ClientInstance() {
    /**
     * The data directory injected into the loaded [VelocityMain] plugin instance.
     */
    override val dataPath: Path get() = plugin.dataPath
}

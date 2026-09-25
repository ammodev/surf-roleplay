package dev.slne.surf.roleplay.velocity

import com.google.auto.service.AutoService
import dev.slne.surf.roleplay.core.client.common.ClientInstance
import java.nio.file.Path

@AutoService(VelocityClientInstance::class)
class VelocityClientInstance : ClientInstance() {
    override val dataPath: Path get() = plugin.dataPath
}
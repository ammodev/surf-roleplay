package dev.slne.surf.roleplay.paper

import com.google.auto.service.AutoService
import dev.slne.surf.roleplay.core.client.common.ClientInstance
import java.nio.file.Path

@AutoService(ClientInstance::class)
class PaperClientInstance : ClientInstance() {
    override val dataPath: Path get() = plugin.dataPath
}
package dev.slne.surf.roleplay.paper

import com.google.auto.service.AutoService
import dev.slne.surf.roleplay.core.client.common.ClientInstance
import java.nio.file.Path

/**
 * The Paper-specific [ClientInstance], which stores its data in the plugin's data folder.
 */
@AutoService(ClientInstance::class)
class PaperClientInstance : ClientInstance() {
    /**
     * The data folder of the loaded [PaperMain] plugin instance.
     */
    override val dataPath: Path get() = plugin.dataPath
}

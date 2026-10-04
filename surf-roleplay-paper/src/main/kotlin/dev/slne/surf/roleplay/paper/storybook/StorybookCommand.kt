package dev.slne.surf.roleplay.paper.storybook

import com.mojang.brigadier.Command
import dev.slne.surf.roleplay.api.client.common.toast.Toast
import dev.slne.surf.roleplay.api.client.paper.toast.ToastService
import dev.slne.surf.roleplay.paper.screen.debug.ScreenDebugCommand
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * The staff command `/rpstorybook`, which opens the storybook of the screen components. It needs
 * the permission [ScreenDebugCommand.PERMISSION] and a player as sender. Clicks and changes in the
 * storybook are reported to the player as toasts.
 */
object StorybookCommand {

    /**
     * Registers the command.
     *
     * @param plugin the plugin that owns the command
     */
    fun register(plugin: Plugin) {
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                Commands.literal("rpstorybook")
                    .requires { it.sender.hasPermission(ScreenDebugCommand.PERMISSION) && it.executor is Player }
                    .executes { context ->
                        open(context.source.executor as Player)
                        Command.SINGLE_SUCCESS
                    }
                    .build(),
                "Öffnet das Storybook der Bildschirm-Komponenten",
            )
        }
    }

    /**
     * The bundled Lucide icons, loaded on first use.
     */
    private val catalog: LucideCatalog by lazy { LucideCatalog.load() }

    /**
     * Opens a new storybook for a player.
     *
     * @param player the player
     */
    private fun open(player: Player) {
        val report: (String) -> Unit = { text -> ToastService.show(player, Toast(Component.text(text))) }
        StorybookPage(player.uniqueId, report) { gallery -> storybookStories(catalog, gallery) }.open(player)
    }
}

package dev.slne.surf.roleplay.paper.screen.debug

import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * The staff command `/rpscreen`, which opens the demo screens of the screen framework.
 *
 * `/rpscreen` opens the generic demo screen, `/rpscreen inputs`, `/rpscreen display`,
 * `/rpscreen overlays` and `/rpscreen navigation` the pages of the input, display, overlay and
 * navigation components, and `/rpscreen counter` the typed debug counter. The command needs the permission [PERMISSION] and a player as sender.
 */
object ScreenDebugCommand {

    /**
     * The permission needed to run the command.
     */
    const val PERMISSION: String = "surf.roleplay.command.rpscreen"

    /**
     * Registers the command.
     *
     * @param plugin the plugin that owns the command
     */
    fun register(plugin: Plugin) {
        val screens = DebugScreens(plugin)
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                Commands.literal("rpscreen")
                    .requires { it.sender.hasPermission(PERMISSION) && it.executor is Player }
                    .executes { context ->
                        screens.openDemo(context.source.executor as Player)
                        Command.SINGLE_SUCCESS
                    }
                    .then(
                        Commands.literal("inputs").executes { context ->
                            InputsDemo.open(context.source.executor as Player)
                            Command.SINGLE_SUCCESS
                        },
                    )
                    .then(
                        Commands.literal("display").executes { context ->
                            DisplayDemo.open(context.source.executor as Player)
                            Command.SINGLE_SUCCESS
                        },
                    )
                    .then(
                        Commands.literal("overlays").executes { context ->
                            OverlaysDemo.open(context.source.executor as Player)
                            Command.SINGLE_SUCCESS
                        },
                    )
                    .then(
                        Commands.literal("navigation").executes { context ->
                            NavigationDemo.open(context.source.executor as Player)
                            Command.SINGLE_SUCCESS
                        },
                    )
                    .then(
                        Commands.literal("counter").executes { context ->
                            screens.openCounter(context.source.executor as Player)
                            Command.SINGLE_SUCCESS
                        },
                    )
                    .build(),
                "Öffnet die Demo-Bildschirme des Bildschirm-Frameworks",
            )
        }
    }
}

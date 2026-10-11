package dev.slne.surf.roleplay.paper.interaction

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.paper.interaction.binding.BindingIndex
import dev.slne.surf.roleplay.paper.interaction.binding.BindingParser
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

/**
 * The logger of the staff command.
 */
private val log = logger()

/**
 * The staff command `/rpinteract`: `reload` reloads interactions.yml, `tag <key>` sets the binding
 * key of the entity the staff member looks at, and `untag` removes it. It needs [PERMISSION] and
 * grants no interaction bypass.
 */
object InteractionCommand {

    /**
     * The permission needed to run the command.
     */
    const val PERMISSION: String = "surf.roleplay.interaction.admin"

    /**
     * How far away the tagged entity may be, in blocks.
     */
    private const val TAG_REACH: Int = 5

    /**
     * Registers the command.
     *
     * @param plugin the plugin that owns the command
     */
    fun register(plugin: Plugin) {
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                Commands.literal("rpinteract")
                    .requires { it.sender.hasPermission(PERMISSION) }
                    .then(Commands.literal("reload").executes { reload(plugin, it.source) })
                    .then(
                        Commands.literal("tag").then(
                            Commands.argument("key", StringArgumentType.word())
                                .executes { tag(it.source, StringArgumentType.getString(it, "key")) },
                        ),
                    )
                    .then(Commands.literal("untag").executes { tag(it.source, null) })
                    .build(),
                "Verwaltet die Interaktions-Bindungen",
            )
        }
    }

    /**
     * Reloads the bindings on the async scheduler and reports the result to the sender; a failed
     * reload is logged and reported as a failure.
     *
     * @param plugin the plugin
     * @param source the command source
     * @return the command result
     */
    private fun reload(plugin: Plugin, source: CommandSourceStack): Int {
        val sender = source.sender
        plugin.server.asyncScheduler.runNow(plugin) {
            try {
                val summary = PaperInteractionService.INSTANCE.reloadBindings(plugin)
                log.atInfo().log("%s reloaded the interaction bindings", sender.name)
                sender.sendMessage(Component.text(summary, NamedTextColor.GREEN))
            } catch (exception: Exception) {
                log.atWarning().withCause(exception).log("Could not reload the interaction bindings")
                sender.sendMessage(Component.text("Die Bindungen konnten nicht neu geladen werden.", NamedTextColor.RED))
            }
        }
        return Command.SINGLE_SUCCESS
    }

    /**
     * Sets or removes the binding key of the entity the player looks at.
     *
     * @param source the command source, which must be a player
     * @param key the new key, or `null` to remove it
     * @return the command result
     */
    private fun tag(source: CommandSourceStack, key: String?): Int {
        val player = source.sender as? Player ?: return fail(source, "Nur Spieler können Entities markieren.")
        if (key != null && !BindingParser.ENTITY_KEY_PATTERN.matches(key)) {
            return fail(source, "Der Schlüssel darf nur a-z, 0-9, Punkt, Unterstrich und Bindestrich enthalten (höchstens 64 Zeichen).")
        }
        val entity = player.getTargetEntity(TAG_REACH)
            ?: return fail(source, "Schau ein Entity an, das höchstens $TAG_REACH Blöcke entfernt ist.")
        if (entity is Player) return fail(source, "Spieler können nicht markiert werden.")
        if (!Bukkit.isOwnedByCurrentRegion(entity)) {
            return fail(source, "Dieses Entity kann gerade nicht bearbeitet werden. Geh näher heran.")
        }
        if (key == null) {
            entity.persistentDataContainer.remove(BindingIndex.ENTITY_KEY)
        } else {
            entity.persistentDataContainer.set(BindingIndex.ENTITY_KEY, PersistentDataType.STRING, key)
        }
        log.atInfo().log("%s set the interaction key of %s to %s", player.name, entity.uniqueId, key ?: "none")
        player.sendMessage(
            Component.text(if (key == null) "Schlüssel entfernt." else "Schlüssel \"$key\" gesetzt.", NamedTextColor.GREEN),
        )
        return Command.SINGLE_SUCCESS
    }

    /**
     * Tells the sender why the command failed.
     *
     * @param source the command source
     * @param message the German message
     * @return the command result
     */
    private fun fail(source: CommandSourceStack, message: String): Int {
        source.sender.sendMessage(Component.text(message, NamedTextColor.RED))
        return 0
    }
}

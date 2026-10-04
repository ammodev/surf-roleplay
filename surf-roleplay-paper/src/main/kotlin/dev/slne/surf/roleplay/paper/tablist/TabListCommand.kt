package dev.slne.surf.roleplay.paper.tablist

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.slne.surf.api.core.util.logger
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.plugin.Plugin

private val log = logger()

/**
 * The pure update of the announcement in the tab list settings.
 */
object TabListAnnouncement {

    /**
     * The longest allowed announcement in characters.
     */
    const val MAX_LENGTH: Int = 120

    /**
     * The outcome of an announcement update.
     */
    sealed interface Result {
        /**
         * The update was accepted.
         *
         * @property config the settings with the new announcement
         */
        data class Updated(val config: TabListConfig) : Result

        /**
         * The text is longer than [MAX_LENGTH] characters and was rejected.
         */
        data object TooLong : Result
    }

    /**
     * Sets or clears the announcement of the settings. The text is trimmed; a blank text clears
     * the announcement.
     *
     * @param config the current settings
     * @param text the new announcement, or `null` to clear it
     * @return the updated settings, or [Result.TooLong] if the trimmed text exceeds [MAX_LENGTH]
     */
    fun set(config: TabListConfig, text: String?): Result {
        val trimmed = text?.trim()?.takeIf { it.isNotEmpty() }
        if (trimmed != null && trimmed.length > MAX_LENGTH) return Result.TooLong
        return Result.Updated(config.copy(announcement = trimmed))
    }
}

/**
 * The staff command `/rptab`. `/rptab announce <text>` sets the announcement line of the tab
 * list and `/rptab announce clear` removes it. It needs the permission [PERMISSION].
 */
object TabListCommand {

    /**
     * The permission needed to run the command.
     */
    const val PERMISSION: String = "surf.roleplay.command.rptab"

    /**
     * The configuration path of the announcement.
     */
    private const val ANNOUNCEMENT_PATH = "tab-list.announcement"

    /**
     * Registers the command.
     *
     * @param plugin the plugin that owns the command
     */
    fun register(plugin: Plugin) {
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                Commands.literal("rptab")
                    .requires { it.sender.hasPermission(PERMISSION) }
                    .then(
                        Commands.literal("announce")
                            .then(Commands.literal("clear").executes { announce(plugin, it, null) })
                            .then(
                                Commands.argument("text", StringArgumentType.greedyString())
                                    .executes { announce(plugin, it, StringArgumentType.getString(it, "text")) },
                            ),
                    )
                    .build(),
                "Verwaltet die Spielerliste des Rollenspiels",
            )
        }
    }

    /**
     * Applies an announcement change: updates the in-memory settings, marks every player's state
     * as changed, saves the plugin configuration asynchronously, logs the staff member and
     * answers the sender.
     *
     * @param plugin the plugin that owns the configuration
     * @param context the command context
     * @param text the new announcement, or `null` to clear it
     * @return the command result
     */
    private fun announce(plugin: Plugin, context: CommandContext<CommandSourceStack>, text: String?): Int {
        val sender = context.source.sender
        val service = PaperTabListService.INSTANCE
        when (val result = TabListAnnouncement.set(service.config, text)) {
            TabListAnnouncement.Result.TooLong -> sender.sendMessage(
                Component.text("Die Ankündigung darf höchstens ${TabListAnnouncement.MAX_LENGTH} Zeichen lang sein.", NamedTextColor.RED),
            )

            is TabListAnnouncement.Result.Updated -> {
                val announcement = result.config.announcement
                service.updateConfig(result.config)
                plugin.config.set(ANNOUNCEMENT_PATH, announcement ?: "")
                plugin.server.asyncScheduler.runNow(plugin) { plugin.saveConfig() }
                if (announcement == null) {
                    log.atInfo().log("%s cleared the tab list announcement", sender.name)
                    sender.sendMessage(Component.text("Ankündigung entfernt.", NamedTextColor.GREEN))
                } else {
                    log.atInfo().log("%s set the tab list announcement to %s", sender.name, announcement)
                    sender.sendMessage(Component.text("Ankündigung gesetzt.", NamedTextColor.GREEN))
                }
            }
        }
        return Command.SINGLE_SUCCESS
    }
}

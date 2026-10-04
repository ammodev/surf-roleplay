package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.dsl.Card
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Icon
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Item
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Separator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Small
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.IconTint
import dev.slne.surf.roleplay.api.client.common.screen.ItemSize
import dev.slne.surf.roleplay.api.client.common.screen.ItemVariant
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.Spacing
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.paper.screen.ScreenService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * The page of the screen debug command that shows the home screen of a smartphone: a status bar,
 * a grid of apps, page dots and a dock. Opening an app is reported in chat.
 */
object PhoneDemo {

    /**
     * An app on the home screen.
     *
     * @property id the id of the app, used in the ids of its elements
     * @property name the name shown below the icon
     * @property icon the Lucide name of the icon
     */
    private data class App(val id: String, val name: String, val icon: String)

    /**
     * The apps in the grid.
     */
    private val GRID = listOf(
        App("contacts", "Kontakte", "contact"),
        App("bank", "Bank", "landmark"),
        App("maps", "Karten", "map"),
    )

    /**
     * The apps in the dock.
     */
    private val DOCK = listOf(
        App("phone", "Telefon", "phone"),
        App("messages", "Nachrichten", "message-circle"),
    )

    /**
     * How wide the phone is laid out.
     */
    private const val PHONE_WIDTH = 220

    /**
     * How tall the screen of the phone is laid out, between its frame.
     */
    private const val PHONE_HEIGHT = 420

    /**
     * How wide one app is laid out, with its name.
     */
    private const val APP_WIDTH = 44

    /**
     * Opens the page.
     *
     * @param player the player
     */
    fun open(player: Player) {
        ScreenService.open(player, definition(player::sendMessage, LocalTime.now()))
    }

    /**
     * Builds the page.
     *
     * @param report sends a report of an opened app to the player
     * @param time the time shown in the status bar
     * @return the page
     */
    fun definition(report: (Component) -> Unit, time: LocalTime): ScreenDefinition {
        val opened = ButtonHandler { click ->
            val app = (GRID + DOCK).first { it.id == click.buttonId.removePrefix("app_") }
            report(Component.text("${app.name} geöffnet", NamedTextColor.GREEN))
        }
        return Screen(Component.text("Handy"), theme = ScreenThemes.DEFAULT, variant = ScreenVariant.DARK) {
            Card(width = ElementSize.fixed(PHONE_WIDTH), id = "phone") {
                Column(width = ElementSize.grow(), height = ElementSize.fixed(PHONE_HEIGHT), gap = 10, padding = Spacing(4, 10, 4, 10), crossAlign = Alignment.STRETCH, id = "phone_screen") {
                    statusBar(time)
                    Row(width = ElementSize.grow(), gap = 6, mainAlign = Alignment.START, id = "grid") {
                        GRID.forEach { app(it, opened) }
                    }
                    Column(height = ElementSize.grow(), id = "spacer") {}
                    Row(width = ElementSize.grow(), gap = 4, mainAlign = Alignment.CENTER, id = "page_dots") {
                        Icon("circle", size = 6, tint = IconTint.FOREGROUND, id = "page_dot_1")
                        Icon("circle", size = 6, tint = IconTint.MUTED, id = "page_dot_2")
                    }
                    Item(variant = ItemVariant.MUTED, size = ItemSize.SM, id = "dock") {
                        Row(width = ElementSize.grow(), gap = 6, mainAlign = Alignment.CENTER, id = "dock_apps") {
                            DOCK.forEach { app(it, opened) }
                        }
                    }
                    Row(width = ElementSize.grow(), mainAlign = Alignment.CENTER, id = "home_indicator") {
                        Column(width = ElementSize.fixed(70), id = "home_indicator_bar") { Separator(id = "home_indicator_line") }
                    }
                }
            }
        }
    }

    /**
     * Adds the status bar: the time on the left, the notch in the middle, and signal, network
     * and battery on the right.
     *
     * @param time the time shown
     */
    private fun ComponentScope.statusBar(time: LocalTime) {
        Row(width = ElementSize.grow(), gap = 4, crossAlign = Alignment.CENTER, id = "status_bar") {
            Small(Component.text(time.format(DateTimeFormatter.ofPattern("HH:mm"))), id = "status_time")
            Row(width = ElementSize.grow(), id = "status_left_space") {}
            Item(variant = ItemVariant.MUTED, size = ItemSize.SM, id = "status_island") {
                Column(width = ElementSize.fixed(40), id = "status_island_body") {}
            }
            Row(width = ElementSize.grow(), id = "status_right_space") {}
            Icon("signal", size = 10, id = "status_signal")
            Icon("wifi", size = 10, id = "status_wifi")
            Icon("battery-full", size = 12, id = "status_battery")
        }
    }

    /**
     * Adds an app: a clickable tile with its icon and its name below.
     *
     * @param app the app
     * @param opened the handler run when the app is clicked
     */
    private fun ComponentScope.app(app: App, opened: ButtonHandler) {
        Column(width = ElementSize.fixed(APP_WIDTH), gap = 3, crossAlign = Alignment.CENTER, id = "app_${app.id}_cell") {
            Item(variant = ItemVariant.OUTLINE, size = ItemSize.SM, onClick = opened, id = "app_${app.id}") {
                Row(width = ElementSize.grow(), mainAlign = Alignment.CENTER, id = "app_${app.id}_icon_row") {
                    Icon(app.icon, size = 20, id = "app_${app.id}_icon")
                }
            }
            Small(Component.text(app.name), id = "app_${app.id}_name")
        }
    }
}

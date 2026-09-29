package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ElementsBuilder
import dev.slne.surf.roleplay.api.client.common.screen.IconTint
import dev.slne.surf.roleplay.api.client.common.screen.ItemSize
import dev.slne.surf.roleplay.api.client.common.screen.ItemVariant
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.Spacing
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.card
import dev.slne.surf.roleplay.api.client.common.screen.item
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.common.screen.separator
import dev.slne.surf.roleplay.api.client.common.screen.text
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
        return screen(Component.text("Handy")) {
            theme = ScreenThemes.DEFAULT
            variant = ScreenVariant.DARK
            card("phone", width = ElementSize.fixed(PHONE_WIDTH)) {
                column("phone_screen", width = ElementSize.grow(), height = ElementSize.fixed(PHONE_HEIGHT), gap = 10, padding = Spacing(4, 10, 4, 10), crossAlign = Alignment.STRETCH) {
                    statusBar(time)
                    row("grid", width = ElementSize.grow(), gap = 6, mainAlign = Alignment.START) {
                        GRID.forEach { app(it, opened) }
                    }
                    column("spacer", height = ElementSize.grow()) {}
                    row("page_dots", width = ElementSize.grow(), gap = 4, mainAlign = Alignment.CENTER) {
                        icon("page_dot_1", "circle", size = 6, tint = IconTint.FOREGROUND)
                        icon("page_dot_2", "circle", size = 6, tint = IconTint.MUTED)
                    }
                    item("dock", variant = ItemVariant.MUTED, size = ItemSize.SM) {
                        row("dock_apps", width = ElementSize.grow(), gap = 6, mainAlign = Alignment.CENTER) {
                            DOCK.forEach { app(it, opened) }
                        }
                    }
                    row("home_indicator", width = ElementSize.grow(), mainAlign = Alignment.CENTER) {
                        column("home_indicator_bar", width = ElementSize.fixed(70)) { separator("home_indicator_line") }
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
    private fun ElementsBuilder.statusBar(time: LocalTime) {
        row("status_bar", width = ElementSize.grow(), gap = 4, crossAlign = Alignment.CENTER) {
            text("status_time", Component.text(time.format(DateTimeFormatter.ofPattern("HH:mm"))), TextKind.SMALL)
            row("status_left_space", width = ElementSize.grow()) {}
            item("status_island", variant = ItemVariant.MUTED, size = ItemSize.SM) {
                column("status_island_body", width = ElementSize.fixed(40)) {}
            }
            row("status_right_space", width = ElementSize.grow()) {}
            icon("status_signal", "signal", size = 10)
            icon("status_wifi", "wifi", size = 10)
            icon("status_battery", "battery-full", size = 12)
        }
    }

    /**
     * Adds an app: a clickable tile with its icon and its name below.
     *
     * @param app the app
     * @param opened the handler run when the app is clicked
     */
    private fun ElementsBuilder.app(app: App, opened: ButtonHandler) {
        column("app_${app.id}_cell", width = ElementSize.fixed(APP_WIDTH), gap = 3, crossAlign = Alignment.CENTER) {
            item("app_${app.id}", variant = ItemVariant.OUTLINE, size = ItemSize.SM, onClick = opened) {
                row("app_${app.id}_icon_row", width = ElementSize.grow(), mainAlign = Alignment.CENTER) {
                    icon("app_${app.id}_icon", app.icon, size = 20)
                }
            }
            text("app_${app.id}_name", Component.text(app.name), TextKind.SMALL)
        }
    }
}

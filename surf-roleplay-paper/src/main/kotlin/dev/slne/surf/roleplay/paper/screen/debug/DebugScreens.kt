package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.DropdownChoice
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.Spacing
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.paper.screen.ScreenService
import dev.slne.surf.roleplay.paper.screen.PaperScreenService
import dev.slne.surf.roleplay.protocol.screen.CounterState
import dev.slne.surf.roleplay.protocol.screen.ScreenTypes
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * The demo screens opened by the screen debug command.
 *
 * @property plugin the plugin that schedules the demo's live updates
 */
class DebugScreens(private val plugin: Plugin) {

    /**
     * Opens the generic demo screen, which uses every element kind, patches itself, opens a child
     * screen and shows a live progress bar.
     *
     * @param player the player
     */
    fun openDemo(player: Player) {
        var progressTask: ScheduledTask? = null
        var patches = 0
        var unlocked = false
        lateinit var demo: OpenScreen

        val definition = screen(Component.text("Bildschirm-Demo")) {
            onClose { progressTask?.cancel() }
            column("root", width = ElementSize.fixed(320), gap = 6, crossAlign = Alignment.STRETCH) {
                label("heading", Component.text("Alle Elemente", NamedTextColor.GOLD, TextDecoration.BOLD))
                row("name_row", gap = 6, crossAlign = Alignment.CENTER) {
                    label("name_label", Component.text("Name"), width = ElementSize.fixed(60))
                    textInput("name", placeholder = Component.text("Max Mustermann"), maxLength = 16, required = true, width = ElementSize.grow())
                }
                row("age_row", gap = 6, crossAlign = Alignment.CENTER) {
                    label("age_label", Component.text("Alter"), width = ElementSize.fixed(60))
                    numberInput("age", min = 18, max = 99, required = true, width = ElementSize.grow())
                }
                row("city_row", gap = 6, crossAlign = Alignment.CENTER) {
                    label("city_label", Component.text("Stadt"), width = ElementSize.fixed(60))
                    dropdown("city", CITIES, required = true, width = ElementSize.grow())
                }
                checkbox("rules", Component.text("Ich akzeptiere die Regeln"))
                scrollList("list", height = ElementSize.fixed(60), gap = 2) {
                    for (index in 1..8) {
                        row("row_$index", gap = 4, crossAlign = Alignment.CENTER) {
                            label("row_${index}_label", Component.text("Eintrag $index"), width = ElementSize.grow())
                            button("row_${index}_open", Component.text("Öffnen")) { click -> openEntry(player, click.screen, index) }
                        }
                    }
                }
                row("media", gap = 6, crossAlign = Alignment.CENTER) {
                    image("logo", Key.key("minecraft", "textures/item/diamond.png"), ElementSize.fixed(16), ElementSize.fixed(16))
                    progress("load", 0f, Component.text("0 %"), width = ElementSize.grow())
                }
                row("buttons", gap = 6, padding = Spacing(top = 4), mainAlign = Alignment.END) {
                    button("locked", Component.text("Gesperrt"), enabled = false) { _ ->
                        player.sendMessage(Component.text("Die gesperrte Schaltfläche wurde benutzt.", NamedTextColor.GREEN))
                    }
                    button("patch", Component.text("Ändern")) { _ ->
                        patches++
                        unlocked = !unlocked
                        demo.patch {
                            setText("heading", Component.text("Geändert um ${LocalTime.now().format(TIME)}", NamedTextColor.AQUA))
                            setEnabled("locked", unlocked)
                            setText("locked", Component.text(if (unlocked) "Freigeschaltet" else "Gesperrt"))
                            insert("list", 0, labelElement("patch_$patches", "Neu: Änderung $patches"))
                        }
                    }
                    button("submit", Component.text("Absenden")) { click ->
                        val values = click.values
                        player.sendMessage(
                            Component.text(
                                "Name: ${values.text("name")}, Alter: ${values.number("age") ?: "-"}, " +
                                    "Stadt: ${values.selected("city") ?: "-"}, Regeln: ${if (values.checked("rules") == true) "ja" else "nein"}",
                                NamedTextColor.GREEN,
                            ),
                        )
                    }
                }
            }
        }

        demo = ScreenService.open(player, definition)
        var tick = 0
        progressTask = player.scheduler.runAtFixedRate(plugin, { _ ->
            tick = (tick + 1) % 11
            demo.patch {
                setProgress("load", tick / 10f)
                setText("load", Component.text("${tick * 10} %"))
            }
        }, null, 20L, 20L)
    }

    /**
     * Opens a child screen for one list entry on top of the demo.
     *
     * @param player the player
     * @param parent the demo screen
     * @param index the number of the entry
     */
    private fun openEntry(player: Player, parent: OpenScreen, index: Int) {
        val definition: ScreenDefinition = screen(Component.text("Eintrag $index")) {
            column("entry", gap = 8, crossAlign = Alignment.CENTER) {
                label("entry_text", Component.text("Das ist Eintrag $index."))
                button("back", Component.text("Zurück")) { click -> click.screen.close() }
            }
        }
        ScreenService.open(player, definition, parent)
    }

    /**
     * Opens the typed debug counter, whose buttons change a number held by the server.
     *
     * @param player the player
     */
    fun openCounter(player: Player) {
        var value = 0
        PaperScreenService.INSTANCE.openTyped(player, ScreenTypes.DEBUG_COUNTER, Component.text("Zähler"), CounterState(value)) { screen, action ->
            value += action.delta.coerceIn(-1, 1)
            screen.update(CounterState(value))
        }
    }

    /**
     * Holds the demo's constants.
     */
    private companion object {
        /**
         * The options of the demo's city dropdown.
         */
        val CITIES = listOf(
            DropdownChoice("north", Component.text("Nordhafen")),
            DropdownChoice("south", Component.text("Südstadt")),
            DropdownChoice("old", Component.text("Altstadt")),
        )

        /**
         * The format of the time shown after a patch.
         */
        val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        /**
         * Creates a label element for a patch.
         *
         * @param id the id
         * @param text the plain text
         * @return the element
         */
        fun labelElement(id: String, text: String) =
            LabelElement(id, Component.text(text, NamedTextColor.YELLOW))
    }
}

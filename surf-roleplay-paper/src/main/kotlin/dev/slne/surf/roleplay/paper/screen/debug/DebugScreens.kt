package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Checkbox
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Image
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NumberInput
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Progress
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ScrollList
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.Spacing
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
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
     * Opens the generic demo screen in a theme. The demo uses every element kind, patches itself,
     * opens a child screen, a sheet and a confirmation dialog, shows a live progress bar, and can
     * reopen itself in another theme.
     *
     * @param player the player
     * @param theme the name of the theme to draw the demo with
     * @param variant the light or dark variant of the theme
     */
    fun openDemo(player: Player, theme: String = ScreenThemes.DEFAULT, variant: ScreenVariant = ScreenVariant.DARK) {
        var progressTask: ScheduledTask? = null
        var patches = 0
        var unlocked = false
        lateinit var demo: OpenScreen

        val definition = Screen(Component.text("Bildschirm-Demo"), theme = theme, variant = variant, onClose = { progressTask?.cancel() }) {
            Column(width = ElementSize.fixed(320), gap = 6, crossAlign = Alignment.STRETCH, id = "root") {
                Label(Component.text("Alle Elemente", NamedTextColor.GOLD, TextDecoration.BOLD), icon = "info", id = "heading")
                Row(gap = 6, crossAlign = Alignment.CENTER, id = "theme_row") {
                    Select(THEMES, selected = theme, width = ElementSize.grow(), id = "theme")
                    Select(VARIANTS, selected = variant.name, width = ElementSize.fixed(90), id = "variant")
                    Button(Component.text("Anwenden"), submitsInput = false, icon = "palette", id = "apply_theme") { click ->
                        val chosenTheme = click.values.selected("theme") ?: ScreenThemes.DEFAULT
                        val chosenVariant = click.values.selected("variant")?.let(ScreenVariant::valueOf) ?: ScreenVariant.DARK
                        openDemo(player, chosenTheme, chosenVariant)
                    }
                }
                Row(gap = 6, crossAlign = Alignment.CENTER, id = "name_row") {
                    Label(Component.text("Name"), width = ElementSize.fixed(60), id = "name_label")
                    Input(placeholder = Component.text("Max Mustermann"), maxLength = 16, required = true, width = ElementSize.grow(), icon = "search", id = "name")
                }
                Row(gap = 6, crossAlign = Alignment.CENTER, id = "age_row") {
                    Label(Component.text("Alter"), width = ElementSize.fixed(60), id = "age_label")
                    NumberInput(min = 18, max = 99, required = true, width = ElementSize.grow(), id = "age")
                }
                Row(gap = 6, crossAlign = Alignment.CENTER, id = "city_row") {
                    Label(Component.text("Stadt"), width = ElementSize.fixed(60), id = "city_label")
                    Select(CITIES, required = true, width = ElementSize.grow(), id = "city")
                }
                Checkbox(Component.text("Ich akzeptiere die Regeln"), id = "rules")
                ScrollList(height = ElementSize.fixed(60), gap = 2, id = "list") {
                    for (index in 1..8) {
                        Row(gap = 4, crossAlign = Alignment.CENTER, id = "row_$index") {
                            Label(Component.text("Eintrag $index"), width = ElementSize.grow(), id = "row_${index}_label")
                            Button(Component.text("Öffnen"), submitsInput = false, id = "row_${index}_open") { click -> openEntry(player, click.screen, index) }
                        }
                    }
                }
                Row(gap = 6, crossAlign = Alignment.CENTER, id = "media") {
                    Image(Key.key("minecraft", "textures/item/diamond.png"), ElementSize.fixed(16), ElementSize.fixed(16), id = "logo")
                    Progress(0f, Component.text("0 %"), width = ElementSize.grow(), id = "load")
                }
                Row(gap = 6, padding = Spacing(top = 4), mainAlign = Alignment.END, id = "popups") {
                    Button(Component.text("Details"), submitsInput = false, icon = "panel-right", id = "details") { click ->
                        openDetails(player, click.screen, theme, variant)
                    }
                    Button(Component.text("Löschen"), submitsInput = false, icon = "trash", id = "delete") { click ->
                        ScreenService.confirm(
                            player,
                            click.screen,
                            title = Component.text("Eintrag löschen?"),
                            text = Component.text("Dieser Eintrag wird unwiderruflich gelöscht."),
                            confirmLabel = Component.text("Löschen"),
                            cancelLabel = Component.text("Abbrechen"),
                            destructive = true,
                            onConfirm = { player.sendMessage(Component.text("Der Eintrag wurde gelöscht.", NamedTextColor.GREEN)) },
                            onCancel = { player.sendMessage(Component.text("Das Löschen wurde abgebrochen.", NamedTextColor.YELLOW)) },
                        )
                    }
                }
                Row(gap = 6, mainAlign = Alignment.END, id = "buttons") {
                    Button(Component.text("Gesperrt"), enabled = false, submitsInput = false, icon = "lock", id = "locked") { _ ->
                        player.sendMessage(Component.text("Die gesperrte Schaltfläche wurde benutzt.", NamedTextColor.GREEN))
                    }
                    Button(Component.text("Ändern"), submitsInput = false, icon = "refresh-cw", id = "patch") { _ ->
                        patches++
                        unlocked = !unlocked
                        demo.patch {
                            setText("heading", Component.text("Geändert um ${LocalTime.now().format(TIME)}", NamedTextColor.AQUA))
                            replace("locked") {
                                Button(Component.text(if (unlocked) "Freigeschaltet" else "Gesperrt"), enabled = unlocked, submitsInput = false, icon = if (unlocked) "lock-open" else "lock", id = "locked", onClick = lockedHandler(player))
                            }
                            insert("list", 0, labelElement("patch_$patches", "Neu: Änderung $patches"))
                        }
                    }
                    Button(Component.text("Absenden"), icon = "send", id = "submit") { click ->
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
     * Returns the handler of the demo's unlockable button, which reports its use in chat.
     *
     * @param player the player
     * @return the handler
     */
    private fun lockedHandler(player: Player) = ButtonHandler {
        player.sendMessage(Component.text("Die gesperrte Schaltfläche wurde benutzt.", NamedTextColor.GREEN))
    }

    /**
     * Opens a sheet from the right with details over the demo.
     *
     * @param player the player
     * @param parent the demo screen
     * @param theme the theme of the demo
     * @param variant the variant of the demo's theme
     */
    private fun openDetails(player: Player, parent: OpenScreen, theme: String, variant: ScreenVariant) {
        val definition = Screen(Component.text("Details"), theme = theme, variant = variant) {
            Column(gap = 8, crossAlign = Alignment.STRETCH, id = "details") {
                Label(Component.text("Seitenleiste").decorate(TextDecoration.BOLD), icon = "panel-right", id = "details_heading")
                Label(Component.text("Diese Leiste gleitet von rechts herein."), id = "details_text")
                Label(Component.text("Escape oder ein Klick daneben schließt sie."), id = "details_hint")
                Button(Component.text("Schließen"), submitsInput = false, icon = "x", id = "details_close") { click -> click.screen.close() }
            }
        }
        ScreenService.open(player, definition, parent, ScreenPresentation.SHEET, SheetSide.RIGHT)
    }

    /**
     * Opens a child screen for one list entry on top of the demo.
     *
     * @param player the player
     * @param parent the demo screen
     * @param index the number of the entry
     */
    private fun openEntry(player: Player, parent: OpenScreen, index: Int) {
        val definition: ScreenDefinition = Screen(Component.text("Eintrag $index")) {
            Column(gap = 8, crossAlign = Alignment.CENTER, id = "entry") {
                Label(Component.text("Das ist Eintrag $index."), id = "entry_text")
                Button(Component.text("Zurück"), submitsInput = false, id = "back") { click -> click.screen.close() }
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
         * The options of the demo's city select.
         */
        val CITIES = listOf(
            SelectChoice("north", Component.text("Nordhafen")),
            SelectChoice("south", Component.text("Südstadt")),
            SelectChoice("old", Component.text("Altstadt")),
        )

        /**
         * The themes offered by the demo's theme select.
         */
        val THEMES = listOf(
            SelectChoice(ScreenThemes.DEFAULT, Component.text("Standard")),
            SelectChoice(ScreenThemes.SAR, Component.text("Rettungsdienst")),
            SelectChoice(ScreenThemes.POLICE, Component.text("Polizei")),
        )

        /**
         * The variants offered by the demo's variant select.
         */
        val VARIANTS = listOf(
            SelectChoice(ScreenVariant.DARK.name, Component.text("Dunkel")),
            SelectChoice(ScreenVariant.LIGHT.name, Component.text("Hell")),
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

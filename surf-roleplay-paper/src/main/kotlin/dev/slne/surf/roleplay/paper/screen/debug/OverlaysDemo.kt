package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogSize
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.CalendarMode
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ElementsBuilder
import dev.slne.surf.roleplay.api.client.common.screen.OverlaySide
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.alertDialog
import dev.slne.surf.roleplay.api.client.common.screen.alertDialogAction
import dev.slne.surf.roleplay.api.client.common.screen.alertDialogCancel
import dev.slne.surf.roleplay.api.client.common.screen.alertDialogContent
import dev.slne.surf.roleplay.api.client.common.screen.alertDialogMedia
import dev.slne.surf.roleplay.api.client.common.screen.avatar
import dev.slne.surf.roleplay.api.client.common.screen.calendar
import dev.slne.surf.roleplay.api.client.common.screen.command
import dev.slne.surf.roleplay.api.client.common.screen.commandEmpty
import dev.slne.surf.roleplay.api.client.common.screen.commandGroup
import dev.slne.surf.roleplay.api.client.common.screen.commandInput
import dev.slne.surf.roleplay.api.client.common.screen.commandItem
import dev.slne.surf.roleplay.api.client.common.screen.commandList
import dev.slne.surf.roleplay.api.client.common.screen.commandSeparator
import dev.slne.surf.roleplay.api.client.common.screen.contextMenu
import dev.slne.surf.roleplay.api.client.common.screen.dialog
import dev.slne.surf.roleplay.api.client.common.screen.dialogClose
import dev.slne.surf.roleplay.api.client.common.screen.dialogContent
import dev.slne.surf.roleplay.api.client.common.screen.dialogDescription
import dev.slne.surf.roleplay.api.client.common.screen.dialogFooter
import dev.slne.surf.roleplay.api.client.common.screen.dialogHeader
import dev.slne.surf.roleplay.api.client.common.screen.dialogTitle
import dev.slne.surf.roleplay.api.client.common.screen.drawer
import dev.slne.surf.roleplay.api.client.common.screen.drawerContent
import dev.slne.surf.roleplay.api.client.common.screen.dropdownMenu
import dev.slne.surf.roleplay.api.client.common.screen.hoverCard
import dev.slne.surf.roleplay.api.client.common.screen.hoverCardContent
import dev.slne.surf.roleplay.api.client.common.screen.menuCheckboxItem
import dev.slne.surf.roleplay.api.client.common.screen.menuContent
import dev.slne.surf.roleplay.api.client.common.screen.menuGroup
import dev.slne.surf.roleplay.api.client.common.screen.menuItem
import dev.slne.surf.roleplay.api.client.common.screen.menuLabel
import dev.slne.surf.roleplay.api.client.common.screen.menuRadioGroup
import dev.slne.surf.roleplay.api.client.common.screen.menuRadioItem
import dev.slne.surf.roleplay.api.client.common.screen.menuSeparator
import dev.slne.surf.roleplay.api.client.common.screen.menuSub
import dev.slne.surf.roleplay.api.client.common.screen.menuSubTrigger
import dev.slne.surf.roleplay.api.client.common.screen.menubar
import dev.slne.surf.roleplay.api.client.common.screen.menubarMenu
import dev.slne.surf.roleplay.api.client.common.screen.menubarTrigger
import dev.slne.surf.roleplay.api.client.common.screen.popover
import dev.slne.surf.roleplay.api.client.common.screen.popoverContent
import dev.slne.surf.roleplay.api.client.common.screen.popoverDescription
import dev.slne.surf.roleplay.api.client.common.screen.popoverHeader
import dev.slne.surf.roleplay.api.client.common.screen.popoverTitle
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.common.screen.select
import dev.slne.surf.roleplay.api.client.common.screen.sheet
import dev.slne.surf.roleplay.api.client.common.screen.sheetContent
import dev.slne.surf.roleplay.api.client.common.screen.sheetDescription
import dev.slne.surf.roleplay.api.client.common.screen.sheetFooter
import dev.slne.surf.roleplay.api.client.common.screen.sheetHeader
import dev.slne.surf.roleplay.api.client.common.screen.sheetTitle
import dev.slne.surf.roleplay.api.client.common.screen.text
import dev.slne.surf.roleplay.api.client.common.screen.tooltip
import dev.slne.surf.roleplay.api.client.common.toast.Toast
import dev.slne.surf.roleplay.api.client.common.toast.ToastButton
import dev.slne.surf.roleplay.api.client.common.toast.ToastType
import dev.slne.surf.roleplay.api.client.paper.screen.ScreenService
import dev.slne.surf.roleplay.api.client.paper.toast.ToastService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.time.format.DateTimeFormatter

/**
 * The page of the screen debug command that shows every overlay and menu component, and sends
 * toasts of every type. Clicks are reported in chat.
 */
object OverlaysDemo {

    /**
     * What the page needs from its surroundings.
     *
     * @property report sends a report of a click to the player
     * @property toast shows a toast to the player and returns its id
     * @property later runs a task after a delay in milliseconds, on the player's thread
     * @property reopen opens the page again in another theme and variant
     */
    class Hooks(
        val report: (Component) -> Unit,
        val toast: (Toast) -> String,
        val later: (Long, () -> Unit) -> Unit,
        val reopen: (String, ScreenVariant) -> Unit,
    )

    /**
     * Opens the page.
     *
     * @param player the player
     * @param theme the name of the theme to draw the page with
     * @param variant the light or dark variant of the theme
     */
    fun open(player: Player, theme: String = ScreenThemes.DEFAULT, variant: ScreenVariant = ScreenVariant.DARK) {
        val plugin = JavaPlugin.getProvidingPlugin(OverlaysDemo::class.java)
        val hooks = Hooks(
            report = player::sendMessage,
            toast = { ToastService.show(player, it) },
            later = { millis, task -> player.scheduler.runDelayed(plugin, { task() }, null, (millis / MILLIS_PER_TICK).coerceAtLeast(1)) },
            reopen = { chosenTheme, chosenVariant -> open(player, chosenTheme, chosenVariant) },
        )
        ScreenService.open(player, definition(hooks, theme, variant))
    }

    /**
     * Builds the page.
     *
     * @param hooks what the page needs from its surroundings
     * @param theme the name of the theme
     * @param variant the variant of the theme
     * @return the page
     */
    fun definition(hooks: Hooks, theme: String, variant: ScreenVariant): ScreenDefinition {
        val clicked = ButtonHandler { click -> hooks.report(Component.text("${click.buttonId} geklickt", NamedTextColor.GREEN)) }
        return screen(Component.text("Overlays")) {
            this.theme = theme
            this.variant = variant
            column("root", width = ElementSize.fixed(380), gap = 12, crossAlign = Alignment.STRETCH) {
                row("theme_row", gap = 4, crossAlign = Alignment.CENTER) {
                    select("theme", InputsDemo.THEMES, selected = theme, width = ElementSize.grow())
                    select("variant", InputsDemo.VARIANTS, selected = variant.name, width = ElementSize.fixed(90))
                    button("apply_theme", Component.text("Anwenden"), submitsInput = false, icon = "palette", variant = ButtonVariant.OUTLINE) { click ->
                        hooks.reopen(click.values.selected("theme") ?: ScreenThemes.DEFAULT, click.values.selected("variant")?.let(ScreenVariant::valueOf) ?: ScreenVariant.DARK)
                    }
                }
                floating(clicked)
                menus(clicked)
                commands(clicked)
                modals(clicked)
                toasts(hooks)
            }
        }
    }

    /**
     * Adds a section with a heading.
     *
     * @param id the id of the section
     * @param title the heading
     * @param content the builder of the section's content
     */
    private fun ElementsBuilder.section(id: String, title: String, content: ElementsBuilder.() -> Unit) {
        column(id, gap = 6, crossAlign = Alignment.START) {
            text("${id}_title", Component.text(title), TextKind.H3)
            content()
        }
    }

    /**
     * Adds popovers, a date picker, a hover card and tooltips on every side.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.floating(clicked: ButtonHandler) = section("floating", "Popover, Hover-Karte, Tooltip") {
        row("floating_row", gap = 6, crossAlign = Alignment.CENTER) {
            popover("popover") {
                button("popover_trigger", Component.text("Maße"), submitsInput = false, variant = ButtonVariant.OUTLINE)
                popoverContent("popover_content") {
                    popoverHeader("popover_header") {
                        popoverTitle("popover_title", Component.text("Maße"))
                        popoverDescription("popover_description", Component.text("Lege die Maße der Ebene fest."))
                    }
                    row("popover_width_row", gap = 4, crossAlign = Alignment.CENTER) {
                        label("popover_width_label", Component.text("Breite"), width = ElementSize.fixed(40))
                        textInput("popover_width", value = "100%", width = ElementSize.grow())
                    }
                    button("popover_save", Component.text("Speichern"), submitsInput = false, onClick = clicked)
                }
            }
            popover("date_picker", align = Alignment.START) {
                button("date_trigger", Component.text("Datum wählen"), submitsInput = false, icon = "calendar", variant = ButtonVariant.OUTLINE)
                popoverContent("date_content", width = ElementSize.FIT) {
                    calendar("date", CalendarMode.SINGLE, onChange = { change ->
                        val chosen = change.values.dates("date")?.firstOrNull()
                        change.screen.patch {
                            chosen?.let { setText("date_trigger", Component.text(it.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))) }
                            setOpen("date_picker", false)
                        }
                    })
                }
            }
            hoverCard("hover") {
                button("hover_trigger", Component.text("@ammo"), submitsInput = false, variant = ButtonVariant.LINK)
                hoverCardContent("hover_content") {
                    row("hover_row", gap = 6) {
                        avatar("hover_avatar", Component.text("AM"))
                        column("hover_texts", gap = 2, width = ElementSize.grow()) {
                            text("hover_name", Component.text("@ammo"), TextKind.LARGE)
                            text("hover_bio", Component.text("Baut das Rollenspiel und seine Oberflächen."), TextKind.MUTED)
                        }
                    }
                }
            }
        }
        row("tooltip_row", gap = 6) {
            OverlaySide.entries.forEach { side ->
                tooltip("tooltip_${side.name.lowercase()}", Component.text("Tooltip ${side.name.lowercase()}"), side) {
                    button("tooltip_trigger_${side.name.lowercase()}", Component.text(side.name.lowercase().replaceFirstChar(Char::uppercase)), submitsInput = false, variant = ButtonVariant.OUTLINE)
                }
            }
        }
    }

    /**
     * Adds a dropdown menu, a context menu and a menubar.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.menus(clicked: ButtonHandler) = section("menus", "Menüs") {
        row("menus_row", gap = 6, crossAlign = Alignment.CENTER) {
            dropdownMenu("menu", align = Alignment.START) {
                button("menu_trigger", Component.text("Konto"), submitsInput = false, icon = "user", variant = ButtonVariant.OUTLINE)
                menuContent("menu_content") {
                    menuLabel("menu_label", Component.text("Mein Konto"))
                    menuGroup("menu_group") {
                        menuItem("menu_profile", Component.text("Profil"), icon = "user", shortcut = Component.text("⇧P"), onClick = clicked)
                        menuItem("menu_billing", Component.text("Abrechnung"), icon = "credit-card", onClick = clicked)
                        menuItem("menu_locked", Component.text("Gesperrt"), icon = "lock", enabled = false)
                    }
                    menuSeparator("menu_sep_a")
                    menuCheckboxItem("menu_status", Component.text("Statusleiste"), checked = true) { click ->
                        reportValue(click.buttonId, click.values.checked("menu_status").toString(), clicked, click)
                    }
                    menuRadioGroup("menu_position", "unten", onSelect = { click -> reportValue(click.buttonId, click.values.text("menu_position").orEmpty(), clicked, click) }) {
                        menuRadioItem("menu_position_top", Component.text("Oben"), "oben")
                        menuRadioItem("menu_position_bottom", Component.text("Unten"), "unten")
                    }
                    menuSeparator("menu_sep_b")
                    menuSub("menu_invite") {
                        menuSubTrigger("menu_invite_trigger", Component.text("Einladen"), icon = "user-plus")
                        menuContent("menu_invite_content") {
                            menuItem("menu_invite_mail", Component.text("Per Funk"), icon = "radio", onClick = clicked)
                            menuItem("menu_invite_sms", Component.text("Per Nachricht"), icon = "message-square", onClick = clicked)
                        }
                    }
                    menuSeparator("menu_sep_c")
                    menuItem("menu_logout", Component.text("Abmelden"), icon = "log-out", destructive = true, onClick = clicked)
                }
            }
            contextMenu("context") {
                column("context_area", width = ElementSize.fixed(140), height = ElementSize.fixed(40), mainAlign = Alignment.CENTER, crossAlign = Alignment.CENTER) {
                    text("context_text", Component.text("Rechtsklick hier"), TextKind.MUTED)
                }
                menuContent("context_content") {
                    menuItem("context_back", Component.text("Zurück"), shortcut = Component.text("Alt+←"), onClick = clicked)
                    menuItem("context_reload", Component.text("Neu laden"), shortcut = Component.text("Strg+R"), onClick = clicked)
                    menuSeparator("context_sep")
                    menuCheckboxItem("context_bookmarks", Component.text("Lesezeichen zeigen"), onToggle = clicked)
                }
            }
        }
        menubar("menubar") {
            listOf("Datei" to listOf("Neu", "Öffnen", "Speichern"), "Bearbeiten" to listOf("Rückgängig", "Wiederholen"), "Ansicht" to listOf("Vollbild")).forEachIndexed { index, (title, entries) ->
                menubarMenu("menubar_$index") {
                    menubarTrigger("menubar_${index}_trigger", Component.text(title))
                    menuContent("menubar_${index}_content") {
                        entries.forEachIndexed { entry, label -> menuItem("menubar_${index}_$entry", Component.text(label), onClick = clicked) }
                    }
                }
            }
        }
    }

    /**
     * Reports the value an input menu entry fired with.
     *
     * @param id the id of the entry
     * @param value the value
     * @param clicked the handler that reports clicks
     * @param click the click
     */
    private fun reportValue(id: String, value: String, clicked: ButtonHandler, click: dev.slne.surf.roleplay.api.client.common.screen.ScreenClick) {
        clicked.onClick(click)
        click.screen.patch { setText("command_title", Component.text("Zuletzt: $id = $value")) }
    }

    /**
     * Adds a command menu and a command dialog.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.commands(clicked: ButtonHandler) = section("commands", "Befehle") {
        text("command_title", Component.text("Tippe, um zu filtern."), TextKind.MUTED)
        command("command") {
            commandInput("command_input", Component.text("Befehl oder Suche eingeben …"))
            commandList("command_list") {
                commandEmpty("command_empty", Component.text("Keine Ergebnisse."))
                commandGroup("command_suggestions", Component.text("Vorschläge")) {
                    commandItem("command_calendar", Component.text("Kalender"), icon = "calendar", keywords = listOf("termin"), onClick = clicked)
                    commandItem("command_search", Component.text("Personen suchen"), icon = "search", onClick = clicked)
                    commandItem("command_calculator", Component.text("Rechner"), icon = "calculator", enabled = false)
                }
                commandSeparator("command_sep")
                commandGroup("command_settings", Component.text("Einstellungen")) {
                    commandItem("command_profile", Component.text("Profil"), icon = "user", shortcut = Component.text("⌘P"), onClick = clicked)
                    commandItem("command_mail", Component.text("Funk"), icon = "radio", shortcut = Component.text("⌘F"), onClick = clicked)
                }
            }
        }
        dialog("command_dialog") {
            button("command_dialog_trigger", Component.text("Befehlsdialog öffnen"), submitsInput = false, icon = "terminal", variant = ButtonVariant.OUTLINE)
            dialogContent("command_dialog_content", showCloseButton = false) {
                command("command_in_dialog") {
                    commandInput("command_in_dialog_input", Component.text("Befehl eingeben …"))
                    commandList("command_in_dialog_list") {
                        commandEmpty("command_in_dialog_empty", Component.text("Keine Ergebnisse."))
                        commandItem("command_in_dialog_help", Component.text("Hilfe"), icon = "circle-help", onClick = clicked)
                        commandItem("command_in_dialog_settings", Component.text("Einstellungen"), icon = "settings", onClick = clicked)
                    }
                }
            }
        }
    }

    /**
     * Adds a dialog, alert dialogs in both sizes, sheets on every side and drawers.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.modals(clicked: ButtonHandler) = section("modals", "Dialoge, Sheets, Drawer") {
        row("dialogs_row", gap = 6) {
            dialog("dialog") {
                button("dialog_trigger", Component.text("Profil bearbeiten"), submitsInput = false, variant = ButtonVariant.OUTLINE)
                dialogContent("dialog_content") {
                    dialogHeader("dialog_header") {
                        dialogTitle("dialog_title", Component.text("Profil bearbeiten"))
                        dialogDescription("dialog_description", Component.text("Ändere hier dein Profil. Klicke auf Speichern, wenn du fertig bist."))
                    }
                    textInput("dialog_name", value = "Max Mustermann", width = ElementSize.grow())
                    dialogFooter("dialog_footer") {
                        dialogClose("dialog_close") { button("dialog_cancel", Component.text("Abbrechen"), submitsInput = false, variant = ButtonVariant.OUTLINE) }
                        dialogClose("dialog_save_close") { button("dialog_save", Component.text("Speichern"), submitsInput = false, onClick = clicked) }
                    }
                }
            }
            alertDialog("alert") {
                button("alert_trigger", Component.text("Akte löschen"), submitsInput = false, variant = ButtonVariant.DESTRUCTIVE)
                alertDialogContent("alert_content") {
                    dialogHeader("alert_header") {
                        dialogTitle("alert_title", Component.text("Bist du sicher?"))
                        dialogDescription("alert_description", Component.text("Das kann nicht rückgängig gemacht werden. Die Akte wird dauerhaft gelöscht."))
                    }
                    dialogFooter("alert_footer") {
                        alertDialogCancel("alert_cancel", Component.text("Abbrechen"))
                        alertDialogAction("alert_confirm", Component.text("Löschen"), ButtonVariant.DESTRUCTIVE, clicked)
                    }
                }
            }
            alertDialog("alert_small") {
                button("alert_small_trigger", Component.text("Klein"), submitsInput = false, variant = ButtonVariant.OUTLINE)
                alertDialogContent("alert_small_content", AlertDialogSize.SM) {
                    dialogHeader("alert_small_header") {
                        alertDialogMedia("alert_small_media", "bluetooth")
                        dialogTitle("alert_small_title", Component.text("Verbindung erlauben?"))
                        dialogDescription("alert_small_description", Component.text("Das Funkgerät möchte sich verbinden."))
                    }
                    dialogFooter("alert_small_footer") {
                        alertDialogCancel("alert_small_cancel", Component.text("Nein"))
                        alertDialogAction("alert_small_confirm", Component.text("Erlauben"), onClick = clicked)
                    }
                }
            }
        }
        row("sheets_row", gap = 6) {
            OverlaySide.entries.forEach { side ->
                val name = side.name.lowercase()
                sheet("sheet_$name") {
                    button("sheet_${name}_trigger", Component.text("Sheet $name"), submitsInput = false, variant = ButtonVariant.OUTLINE)
                    sheetContent("sheet_${name}_content", side) {
                        sheetHeader("sheet_${name}_header") {
                            sheetTitle("sheet_${name}_title", Component.text("Filter"))
                            sheetDescription("sheet_${name}_description", Component.text("Grenze die Liste der Einsätze ein."))
                        }
                        column("sheet_${name}_body", gap = 4, padding = dev.slne.surf.roleplay.api.client.common.screen.Spacing(0, 8, 0, 8), crossAlign = Alignment.STRETCH) {
                            textInput("sheet_${name}_query", placeholder = Component.text("Stichwort"))
                        }
                        sheetFooter("sheet_${name}_footer") {
                            dialogClose("sheet_${name}_close") { button("sheet_${name}_apply", Component.text("Anwenden"), submitsInput = false, width = ElementSize.grow(), onClick = clicked) }
                        }
                    }
                }
            }
        }
        row("drawers_row", gap = 6) {
            listOf(OverlaySide.BOTTOM, OverlaySide.TOP).forEach { side ->
                val name = side.name.lowercase()
                drawer("drawer_$name") {
                    button("drawer_${name}_trigger", Component.text("Drawer $name"), submitsInput = false, variant = ButtonVariant.OUTLINE)
                    drawerContent("drawer_${name}_content", side) {
                        sheetHeader("drawer_${name}_header") {
                            sheetTitle("drawer_${name}_title", Component.text("Tagesziel"))
                            sheetDescription("drawer_${name}_description", Component.text("Lege dein Tagesziel für Streifen fest."))
                        }
                        sheetFooter("drawer_${name}_footer") {
                            dialogClose("drawer_${name}_close") { button("drawer_${name}_submit", Component.text("Übernehmen"), submitsInput = false, width = ElementSize.grow(), onClick = clicked) }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds buttons that send toasts of every type.
     *
     * @param hooks what the page needs from its surroundings
     */
    private fun ElementsBuilder.toasts(hooks: Hooks) = section("toasts", "Toasts") {
        row("toasts_row_a", gap = 4) {
            button("toast_default", Component.text("Standard"), submitsInput = false, variant = ButtonVariant.OUTLINE) {
                hooks.toast(Toast(Component.text("Termin angelegt"), Component.text("Freitag, 18:00 Uhr")))
            }
            button("toast_success", Component.text("Erfolg"), submitsInput = false, variant = ButtonVariant.OUTLINE) {
                hooks.toast(Toast(Component.text("Gespeichert"), type = ToastType.SUCCESS))
            }
            button("toast_info", Component.text("Info"), submitsInput = false, variant = ButtonVariant.OUTLINE) {
                hooks.toast(Toast(Component.text("Neue Dienstanweisung"), Component.text("Bitte bis Schichtbeginn lesen."), ToastType.INFO))
            }
            button("toast_warning", Component.text("Warnung"), submitsInput = false, variant = ButtonVariant.OUTLINE) {
                hooks.toast(Toast(Component.text("Tank fast leer"), type = ToastType.WARNING))
            }
            button("toast_error", Component.text("Fehler"), submitsInput = false, variant = ButtonVariant.OUTLINE) {
                hooks.toast(Toast(Component.text("Zahlung fehlgeschlagen"), Component.text("Konto nicht gedeckt."), ToastType.ERROR))
            }
        }
        row("toasts_row_b", gap = 4) {
            button("toast_loading", Component.text("Laden"), submitsInput = false, variant = ButtonVariant.OUTLINE) {
                val id = hooks.toast(Toast(Component.text("Akte wird hochgeladen …"), type = ToastType.LOADING, id = "demo-upload"))
                hooks.later(LOADING_MILLIS) { hooks.toast(Toast(Component.text("Akte hochgeladen"), type = ToastType.SUCCESS, id = id)) }
            }
            button("toast_action", Component.text("Mit Aktion"), submitsInput = false, variant = ButtonVariant.OUTLINE) {
                hooks.toast(
                    Toast(
                        Component.text("Eingehender Funkspruch"),
                        Component.text("Streife 12 ruft."),
                        action = ToastButton(Component.text("Annehmen")) { hooks.report(Component.text("Funkspruch angenommen", NamedTextColor.GREEN)) },
                        cancel = ToastButton(Component.text("Ablehnen")) { hooks.report(Component.text("Funkspruch abgelehnt", NamedTextColor.RED)) },
                        durationMillis = 10_000,
                    ),
                )
            }
            button("toast_sticky", Component.text("Bleibt"), submitsInput = false, variant = ButtonVariant.OUTLINE) {
                hooks.toast(Toast(Component.text("Wartungsarbeiten um 22:00 Uhr"), durationMillis = 0, closeButton = true))
            }
        }
        text("toasts_hint", Component.text("Halte die linke Alt-Taste gedrückt, um Toasts ohne geöffneten Bildschirm anzuklicken."), TextKind.MUTED)
    }

    /**
     * How long the loading toast waits before it turns into a success, in milliseconds.
     */
    private const val LOADING_MILLIS: Long = 2500

    /**
     * The length of a server tick in milliseconds.
     */
    private const val MILLIS_PER_TICK: Long = 50
}

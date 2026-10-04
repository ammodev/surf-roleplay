package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialog
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogAction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogCancel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogMedia
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Avatar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Calendar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Command
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandEmpty
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandInput
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandList
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ContextMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Dialog
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogClose
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Drawer
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DrawerContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H3
import dev.slne.surf.roleplay.api.client.common.screen.dsl.HoverCard
import dev.slne.surf.roleplay.api.client.common.screen.dsl.HoverCardContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Large
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuCheckboxItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuLabel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuRadioGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuRadioItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuSub
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuSubTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Menubar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenubarMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenubarTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Muted
import dev.slne.surf.roleplay.api.client.common.screen.dsl.OverlayContainer
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Popover
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Sheet
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Tooltip
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogSize
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.CalendarMode
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.OverlaySide
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
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
        return Screen(Component.text("Overlays"), theme = theme, variant = variant) {
            Column(width = ElementSize.fixed(380), gap = 12, crossAlign = Alignment.STRETCH, id = "root") {
                Row(gap = 4, crossAlign = Alignment.CENTER, id = "theme_row") {
                    Select(InputsDemo.THEMES, selected = theme, width = ElementSize.grow(), id = "theme")
                    Select(InputsDemo.VARIANTS, selected = variant.name, width = ElementSize.fixed(90), id = "variant")
                    Button(Component.text("Anwenden"), submitsInput = false, icon = "palette", variant = ButtonVariant.OUTLINE, id = "apply_theme") { click ->
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
    private fun ComponentScope.section(id: String, title: String, content: ComponentScope.() -> Unit) {
        Column(gap = 6, crossAlign = Alignment.START, id = id) {
            H3(Component.text(title), id = "${id}_title")
            content()
        }
    }

    /**
     * Adds popovers, a date picker, a hover card and tooltips on every side.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ComponentScope.floating(clicked: ButtonHandler) = section("floating", "Popover, Hover-Karte, Tooltip") {
        Row(gap = 6, crossAlign = Alignment.CENTER, id = "floating_row") {
            Popover(id = "popover") {
                Button(Component.text("Maße"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "popover_trigger")
                PopoverContent(id = "popover_content") {
                    PopoverHeader(id = "popover_header") {
                        PopoverTitle(Component.text("Maße"), id = "popover_title")
                        PopoverDescription(Component.text("Lege die Maße der Ebene fest."), id = "popover_description")
                    }
                    Row(gap = 4, crossAlign = Alignment.CENTER, id = "popover_width_row") {
                        Label(Component.text("Breite"), width = ElementSize.fixed(40), id = "popover_width_label")
                        Input(value = "100%", width = ElementSize.grow(), id = "popover_width")
                    }
                    Button(Component.text("Speichern"), submitsInput = false, onClick = clicked, id = "popover_save")
                }
            }
            Popover(align = Alignment.START, id = "date_picker") {
                Button(Component.text("Datum wählen"), submitsInput = false, icon = "calendar", variant = ButtonVariant.OUTLINE, id = "date_trigger")
                PopoverContent(width = ElementSize.FIT, id = "date_content") {
                    Calendar(CalendarMode.SINGLE, onChange = { change ->
                        val chosen = change.values.dates("date")?.firstOrNull()
                        change.screen.patch {
                            chosen?.let { setText("date_trigger", Component.text(it.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))) }
                            setOpen("date_picker", false)
                        }
                    }, id = "date")
                }
            }
            HoverCard(id = "hover") {
                Button(Component.text("@ammo"), submitsInput = false, variant = ButtonVariant.LINK, id = "hover_trigger")
                HoverCardContent(id = "hover_content") {
                    Row(gap = 6, id = "hover_row") {
                        Avatar(Component.text("AM"), id = "hover_avatar")
                        Column(gap = 2, width = ElementSize.grow(), id = "hover_texts") {
                            Large(Component.text("@ammo"), id = "hover_name")
                            Muted(Component.text("Baut das Rollenspiel und seine Oberflächen."), id = "hover_bio")
                        }
                    }
                }
            }
        }
        Row(gap = 6, id = "tooltip_row") {
            OverlaySide.entries.forEach { side ->
                Tooltip(Component.text("Tooltip ${side.name.lowercase()}"), side, id = "tooltip_${side.name.lowercase()}") {
                    Button(Component.text(side.name.lowercase().replaceFirstChar(Char::uppercase)), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "tooltip_trigger_${side.name.lowercase()}")
                }
            }
        }
    }

    /**
     * Adds a dropdown menu, a context menu and a menubar.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ComponentScope.menus(clicked: ButtonHandler) = section("menus", "Menüs") {
        Row(gap = 6, crossAlign = Alignment.CENTER, id = "menus_row") {
            DropdownMenu(align = Alignment.START, id = "menu") {
                Button(Component.text("Konto"), submitsInput = false, icon = "user", variant = ButtonVariant.OUTLINE, id = "menu_trigger")
                MenuContent(id = "menu_content") {
                    MenuLabel(Component.text("Mein Konto"), id = "menu_label")
                    MenuGroup(id = "menu_group") {
                        MenuItem(Component.text("Profil"), icon = "user", shortcut = Component.text("⇧P"), onClick = clicked, id = "menu_profile")
                        MenuItem(Component.text("Abrechnung"), icon = "credit-card", onClick = clicked, id = "menu_billing")
                        MenuItem(Component.text("Gesperrt"), icon = "lock", enabled = false, id = "menu_locked")
                    }
                    MenuSeparator(id = "menu_sep_a")
                    MenuCheckboxItem(Component.text("Statusleiste"), checked = true, id = "menu_status") { click ->
                        reportValue(click.buttonId, click.values.checked("menu_status").toString(), clicked, click)
                    }
                    MenuRadioGroup("unten", onSelect = { click -> reportValue(click.buttonId, click.values.text("menu_position").orEmpty(), clicked, click) }, id = "menu_position") {
                        MenuRadioItem(Component.text("Oben"), "oben", id = "menu_position_top")
                        MenuRadioItem(Component.text("Unten"), "unten", id = "menu_position_bottom")
                    }
                    MenuSeparator(id = "menu_sep_b")
                    MenuSub(id = "menu_invite") {
                        MenuSubTrigger(Component.text("Einladen"), icon = "user-plus", id = "menu_invite_trigger")
                        MenuContent(id = "menu_invite_content") {
                            MenuItem(Component.text("Per Funk"), icon = "radio", onClick = clicked, id = "menu_invite_mail")
                            MenuItem(Component.text("Per Nachricht"), icon = "message-square", onClick = clicked, id = "menu_invite_sms")
                        }
                    }
                    MenuSeparator(id = "menu_sep_c")
                    MenuItem(Component.text("Abmelden"), icon = "log-out", destructive = true, onClick = clicked, id = "menu_logout")
                }
            }
            ContextMenu(id = "context") {
                Column(width = ElementSize.fixed(140), height = ElementSize.fixed(40), mainAlign = Alignment.CENTER, crossAlign = Alignment.CENTER, id = "context_area") {
                    Muted(Component.text("Rechtsklick hier"), id = "context_text")
                }
                MenuContent(id = "context_content") {
                    MenuItem(Component.text("Zurück"), shortcut = Component.text("Alt+←"), onClick = clicked, id = "context_back")
                    MenuItem(Component.text("Neu laden"), shortcut = Component.text("Strg+R"), onClick = clicked, id = "context_reload")
                    MenuSeparator(id = "context_sep")
                    MenuCheckboxItem(Component.text("Lesezeichen zeigen"), onToggle = clicked, id = "context_bookmarks")
                }
            }
        }
        Menubar(id = "menubar") {
            listOf("Datei" to listOf("Neu", "Öffnen", "Speichern"), "Bearbeiten" to listOf("Rückgängig", "Wiederholen"), "Ansicht" to listOf("Vollbild")).forEachIndexed { index, (title, entries) ->
                MenubarMenu(id = "menubar_$index") {
                    MenubarTrigger(Component.text(title), id = "menubar_${index}_trigger")
                    MenuContent(id = "menubar_${index}_content") {
                        entries.forEachIndexed { entry, label -> MenuItem(Component.text(label), onClick = clicked, id = "menubar_${index}_$entry") }
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
    private fun ComponentScope.commands(clicked: ButtonHandler) = section("commands", "Befehle") {
        Muted(Component.text("Tippe, um zu filtern."), id = "command_title")
        Command(id = "command") {
            CommandInput(Component.text("Befehl oder Suche eingeben …"), id = "command_input")
            CommandList(id = "command_list") {
                CommandEmpty(Component.text("Keine Ergebnisse."), id = "command_empty")
                CommandGroup(Component.text("Vorschläge"), id = "command_suggestions") {
                    CommandItem(Component.text("Kalender"), icon = "calendar", keywords = listOf("termin"), onClick = clicked, id = "command_calendar")
                    CommandItem(Component.text("Personen suchen"), icon = "search", onClick = clicked, id = "command_search")
                    CommandItem(Component.text("Rechner"), icon = "calculator", enabled = false, id = "command_calculator")
                }
                CommandSeparator(id = "command_sep")
                CommandGroup(Component.text("Einstellungen"), id = "command_settings") {
                    CommandItem(Component.text("Profil"), icon = "user", shortcut = Component.text("⌘P"), onClick = clicked, id = "command_profile")
                    CommandItem(Component.text("Funk"), icon = "radio", shortcut = Component.text("⌘F"), onClick = clicked, id = "command_mail")
                }
            }
        }
        Dialog(id = "command_dialog") {
            Button(Component.text("Befehlsdialog öffnen"), submitsInput = false, icon = "terminal", variant = ButtonVariant.OUTLINE, id = "command_dialog_trigger")
            DialogContent(showCloseButton = false, id = "command_dialog_content") {
                Command(id = "command_in_dialog") {
                    CommandInput(Component.text("Befehl eingeben …"), id = "command_in_dialog_input")
                    CommandList(id = "command_in_dialog_list") {
                        CommandEmpty(Component.text("Keine Ergebnisse."), id = "command_in_dialog_empty")
                        CommandItem(Component.text("Hilfe"), icon = "circle-help", onClick = clicked, id = "command_in_dialog_help")
                        CommandItem(Component.text("Einstellungen"), icon = "settings", onClick = clicked, id = "command_in_dialog_settings")
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
    private fun ComponentScope.modals(clicked: ButtonHandler) = section("modals", "Dialoge, Sheets, Drawer") {
        Row(gap = 6, id = "dialogs_row") {
            Dialog(id = "dialog") {
                Button(Component.text("Profil bearbeiten"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "dialog_trigger")
                DialogContent(id = "dialog_content") {
                    DialogHeader(id = "dialog_header") {
                        DialogTitle(Component.text("Profil bearbeiten"), id = "dialog_title")
                        DialogDescription(Component.text("Ändere hier dein Profil. Klicke auf Speichern, wenn du fertig bist."), id = "dialog_description")
                    }
                    Input(value = "Max Mustermann", width = ElementSize.grow(), id = "dialog_name")
                    DialogFooter(id = "dialog_footer") {
                        DialogClose(id = "dialog_close") { Button(Component.text("Abbrechen"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "dialog_cancel") }
                        DialogClose(id = "dialog_save_close") { Button(Component.text("Speichern"), submitsInput = false, onClick = clicked, id = "dialog_save") }
                    }
                }
            }
            AlertDialog(id = "alert") {
                Button(Component.text("Akte löschen"), submitsInput = false, variant = ButtonVariant.DESTRUCTIVE, id = "alert_trigger")
                AlertDialogContent(id = "alert_content") {
                    DialogHeader(id = "alert_header") {
                        DialogTitle(Component.text("Bist du sicher?"), id = "alert_title")
                        DialogDescription(Component.text("Das kann nicht rückgängig gemacht werden. Die Akte wird dauerhaft gelöscht."), id = "alert_description")
                    }
                    DialogFooter(id = "alert_footer") {
                        AlertDialogCancel(Component.text("Abbrechen"), id = "alert_cancel")
                        AlertDialogAction(Component.text("Löschen"), ButtonVariant.DESTRUCTIVE, onClick = clicked, id = "alert_confirm")
                    }
                }
            }
            AlertDialog(id = "alert_small") {
                Button(Component.text("Klein"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "alert_small_trigger")
                AlertDialogContent(AlertDialogSize.SM, id = "alert_small_content") {
                    DialogHeader(id = "alert_small_header") {
                        AlertDialogMedia("bluetooth", id = "alert_small_media")
                        DialogTitle(Component.text("Verbindung erlauben?"), id = "alert_small_title")
                        DialogDescription(Component.text("Das Funkgerät möchte sich verbinden."), id = "alert_small_description")
                    }
                    DialogFooter(id = "alert_small_footer") {
                        AlertDialogCancel(Component.text("Nein"), id = "alert_small_cancel")
                        AlertDialogAction(Component.text("Erlauben"), onClick = clicked, id = "alert_small_confirm")
                    }
                }
            }
        }
        Row(gap = 6, id = "sheets_row") {
            OverlaySide.entries.forEach { side ->
                val name = side.name.lowercase()
                Sheet(id = "sheet_$name") {
                    Button(Component.text("Sheet $name"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "sheet_${name}_trigger")
                    SheetContent(side, id = "sheet_${name}_content") {
                        SheetHeader(id = "sheet_${name}_header") {
                            SheetTitle(Component.text("Filter"), id = "sheet_${name}_title")
                            SheetDescription(Component.text("Grenze die Liste der Einsätze ein."), id = "sheet_${name}_description")
                        }
                        Column(gap = 4, padding = dev.slne.surf.roleplay.api.client.common.screen.Spacing(0, 8, 0, 8), crossAlign = Alignment.STRETCH, id = "sheet_${name}_body") {
                            Input(placeholder = Component.text("Stichwort"), id = "sheet_${name}_query")
                        }
                        SheetFooter(id = "sheet_${name}_footer") {
                            DialogClose(id = "sheet_${name}_close") { Button(Component.text("Anwenden"), submitsInput = false, width = ElementSize.grow(), onClick = clicked, id = "sheet_${name}_apply") }
                        }
                    }
                }
            }
        }
        Row(gap = 8, id = "phone_row") {
            OverlayContainer(ElementSize.fixed(120), ElementSize.fixed(180), id = "phone") {
                Column(width = ElementSize.grow(), height = ElementSize.grow(), gap = 6, padding = dev.slne.surf.roleplay.api.client.common.screen.Spacing(8, 8, 8, 8), crossAlign = Alignment.STRETCH, id = "phone_screen") {
                    Large(Component.text("Handy"), id = "phone_title")
                    Drawer(id = "phone_drawer") {
                        Button(Component.text("Nachrichten"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "phone_drawer_trigger")
                        DrawerContent(id = "phone_drawer_content") {
                            SheetHeader(id = "phone_drawer_header") {
                                SheetTitle(Component.text("Neue Nachricht"), id = "phone_drawer_title")
                                SheetDescription(Component.text("Von: Leitstelle"), id = "phone_drawer_description")
                            }
                        }
                    }
                }
            }
            Muted(Component.text("Dialoge, Sheets und Drawer bleiben in ihrem Overlay-Container, hier einem Handy."), width = ElementSize.grow(), id = "phone_hint")
        }
        Row(gap = 6, id = "drawers_row") {
            listOf(OverlaySide.BOTTOM, OverlaySide.TOP).forEach { side ->
                val name = side.name.lowercase()
                Drawer(id = "drawer_$name") {
                    Button(Component.text("Drawer $name"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "drawer_${name}_trigger")
                    DrawerContent(side, id = "drawer_${name}_content") {
                        SheetHeader(id = "drawer_${name}_header") {
                            SheetTitle(Component.text("Tagesziel"), id = "drawer_${name}_title")
                            SheetDescription(Component.text("Lege dein Tagesziel für Streifen fest."), id = "drawer_${name}_description")
                        }
                        SheetFooter(id = "drawer_${name}_footer") {
                            DialogClose(id = "drawer_${name}_close") { Button(Component.text("Übernehmen"), submitsInput = false, width = ElementSize.grow(), onClick = clicked, id = "drawer_${name}_submit") }
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
    private fun ComponentScope.toasts(hooks: Hooks) = section("toasts", "Toasts") {
        Row(gap = 4, id = "toasts_row_a") {
            Button(Component.text("Standard"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "toast_default") {
                hooks.toast(Toast(Component.text("Termin angelegt"), Component.text("Freitag, 18:00 Uhr")))
            }
            Button(Component.text("Erfolg"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "toast_success") {
                hooks.toast(Toast(Component.text("Gespeichert"), type = ToastType.SUCCESS))
            }
            Button(Component.text("Info"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "toast_info") {
                hooks.toast(Toast(Component.text("Neue Dienstanweisung"), Component.text("Bitte bis Schichtbeginn lesen."), ToastType.INFO))
            }
            Button(Component.text("Warnung"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "toast_warning") {
                hooks.toast(Toast(Component.text("Tank fast leer"), type = ToastType.WARNING))
            }
            Button(Component.text("Fehler"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "toast_error") {
                hooks.toast(Toast(Component.text("Zahlung fehlgeschlagen"), Component.text("Konto nicht gedeckt."), ToastType.ERROR))
            }
        }
        Row(gap = 4, id = "toasts_row_b") {
            Button(Component.text("Laden"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "toast_loading") {
                val id = hooks.toast(Toast(Component.text("Akte wird hochgeladen …"), type = ToastType.LOADING, id = "demo-upload"))
                hooks.later(LOADING_MILLIS) { hooks.toast(Toast(Component.text("Akte hochgeladen"), type = ToastType.SUCCESS, id = id)) }
            }
            Button(Component.text("Mit Aktion"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "toast_action") {
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
            Button(Component.text("Bleibt"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "toast_sticky") {
                hooks.toast(Toast(Component.text("Wartungsarbeiten um 22:00 Uhr"), durationMillis = 0, closeButton = true))
            }
        }
        Muted(Component.text("Halte die linke Alt-Taste gedrückt, um Toasts ohne geöffneten Bildschirm anzuklicken."), id = "toasts_hint")
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

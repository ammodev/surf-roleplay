package dev.slne.surf.roleplay.paper.storybook.stories

import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogSize
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.OverlaySide
import dev.slne.surf.roleplay.api.client.common.screen.Spacing
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialog
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogAction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogCancel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogMedia
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Avatar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Command
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandEmpty
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandInput
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandList
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CommandSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ContextMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ContextMenuCheckboxItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ContextMenuContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ContextMenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ContextMenuSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Dialog
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogClose
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuCheckboxItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuLabel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuRadioGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuRadioItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuSub
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuSubContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuSubTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.HoverCard
import dev.slne.surf.roleplay.api.client.common.screen.dsl.HoverCardContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Large
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Menubar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenubarCheckboxItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenubarContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenubarItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenubarMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenubarSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenubarTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Muted
import dev.slne.surf.roleplay.api.client.common.screen.dsl.OverlayContainer
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Popover
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Sheet
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Tooltip
import dev.slne.surf.roleplay.api.client.common.toast.Toast
import dev.slne.surf.roleplay.api.client.common.toast.ToastButton
import dev.slne.surf.roleplay.api.client.common.toast.ToastType
import dev.slne.surf.roleplay.api.client.paper.toast.ToastService
import dev.slne.surf.roleplay.paper.storybook.Story
import dev.slne.surf.roleplay.paper.storybook.StoryCategory
import dev.slne.surf.roleplay.paper.storybook.StoryContext
import dev.slne.surf.roleplay.paper.storybook.slug
import dev.slne.surf.roleplay.paper.storybook.storySection
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

/**
 * The stories of the overlay and menu components, in sidebar order.
 */
internal val OVERLAY_STORIES: List<Story> = listOf(
    Story("dialog", "Dialog", StoryCategory.OVERLAYS) { dialogStory(it) },
    Story("alert-dialog", "Bestätigungsdialog", StoryCategory.OVERLAYS) { alertDialogStory(it) },
    Story("sheet", "Seitenblatt", StoryCategory.OVERLAYS) { sheetStory(it) },
    Story("popover", "Popover", StoryCategory.OVERLAYS) { popoverStory(it) },
    Story("hover-card", "Hover-Karte", StoryCategory.OVERLAYS) { hoverCardStory() },
    Story("tooltip", "Tooltip", StoryCategory.OVERLAYS) { tooltipStory(it) },
    Story("dropdown-menu", "Dropdown-Menü", StoryCategory.OVERLAYS) { dropdownMenuStory(it) },
    Story("context-menu", "Kontextmenü", StoryCategory.OVERLAYS) { contextMenuStory(it) },
    Story("menubar", "Menüleiste", StoryCategory.OVERLAYS) { menubarStory(it) },
    Story("command", "Befehlsmenü", StoryCategory.OVERLAYS) { commandStory(it) },
    Story("toast", "Toast", StoryCategory.OVERLAYS) { toastStory(it) },
    Story("sonner", "Sonner", StoryCategory.OVERLAYS) { sonnerStory(it) },
)

/**
 * Shows a dialog with and without the close button, and a dialog form.
 *
 * @param context the story context
 */
private fun ComponentScope.dialogStory(context: StoryContext) {
    storySection("Varianten") {
        Row(gap = 6) {
            listOf(true, false).forEach { closeButton ->
                Dialog {
                    Button(if (closeButton) "Mit Schließen-Knopf" else "Ohne Schließen-Knopf", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "dialog_trigger_$closeButton")
                    DialogContent(showCloseButton = closeButton) {
                        DialogHeader { DialogTitle(Component.text("Hinweis")) }
                        DialogFooter { DialogClose { Button("Verstanden", submitsInput = false, id = "dialog_ok_$closeButton", onClick = context.clicked) } }
                    }
                }
            }
        }
    }
    storySection("Beispiel") {
        Dialog {
            Button("Profil bearbeiten", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "dialog_trigger")
            DialogContent {
                DialogHeader {
                    DialogTitle(Component.text("Profil bearbeiten"))
                    DialogDescription(Component.text("Ändere hier dein Profil. Klicke auf Speichern, wenn du fertig bist."))
                }
                Input(value = "Max Mustermann", width = ElementSize.grow(), id = "dialog_name", onChange = context.changed)
                DialogFooter {
                    DialogClose { Button("Abbrechen", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "dialog_cancel", onClick = context.clicked) }
                    DialogClose { Button("Speichern", submitsInput = false, id = "dialog_save", onClick = context.clicked) }
                }
            }
        }
    }
}

/**
 * Shows alert dialogs in every size, with media and a destructive action.
 *
 * @param context the story context
 */
private fun ComponentScope.alertDialogStory(context: StoryContext) {
    storySection("Größen") {
        Row(gap = 6) {
            AlertDialogSize.entries.forEach { size ->
                AlertDialog {
                    Button(size.slug(), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "alert_trigger_${size.slug()}")
                    AlertDialogContent(size) {
                        DialogHeader {
                            AlertDialogMedia("bluetooth")
                            DialogTitle(Component.text("Verbindung erlauben?"))
                            DialogDescription(Component.text("Das Funkgerät möchte sich verbinden."))
                        }
                        DialogFooter {
                            AlertDialogCancel("Nein", id = "alert_cancel_${size.slug()}", onClick = context.clicked)
                            AlertDialogAction("Erlauben", id = "alert_confirm_${size.slug()}", onClick = context.clicked)
                        }
                    }
                }
            }
        }
    }
    storySection("Beispiel") {
        AlertDialog {
            Button("Akte löschen", submitsInput = false, variant = ButtonVariant.DESTRUCTIVE, id = "alert_trigger")
            AlertDialogContent {
                DialogHeader {
                    DialogTitle(Component.text("Bist du sicher?"))
                    DialogDescription(Component.text("Das kann nicht rückgängig gemacht werden. Die Akte wird dauerhaft gelöscht."))
                }
                DialogFooter {
                    AlertDialogCancel("Abbrechen", id = "alert_cancel")
                    AlertDialogAction("Löschen", ButtonVariant.DESTRUCTIVE, id = "alert_confirm", onClick = context.clicked)
                }
            }
        }
    }
}

/**
 * Shows sheets on every side, with and without the close button, and a sheet kept inside an
 * overlay container.
 *
 * @param context the story context
 */
private fun ComponentScope.sheetStory(context: StoryContext) {
    storySection("Seiten") {
        Row(gap = 6) {
            OverlaySide.entries.forEach { side ->
                Sheet {
                    Button(side.slug(), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "sheet_trigger_${side.slug()}")
                    SheetContent(side, showCloseButton = side != OverlaySide.TOP) {
                        SheetHeader {
                            SheetTitle("Filter")
                            SheetDescription("Grenze die Liste der Einsätze ein.")
                        }
                        Column(gap = 4, padding = Spacing(0, 8, 0, 8), crossAlign = Alignment.STRETCH) {
                            Input(placeholder = Component.text("Stichwort"), id = "sheet_query_${side.slug()}", onChange = context.changed)
                        }
                        SheetFooter {
                            DialogClose { Button("Anwenden", submitsInput = false, width = ElementSize.grow(), id = "sheet_apply_${side.slug()}", onClick = context.clicked) }
                        }
                    }
                }
            }
        }
    }
    storySection("Beispiel") {
        Row(gap = 8) {
            OverlayContainer(ElementSize.fixed(120), ElementSize.fixed(180)) {
                Column(width = ElementSize.grow(), height = ElementSize.grow(), gap = 6, padding = Spacing(8, 8, 8, 8), crossAlign = Alignment.STRETCH) {
                    Large("Handy")
                    Sheet {
                        Button("Nachrichten", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "sheet_phone_trigger")
                        SheetContent(OverlaySide.BOTTOM, showCloseButton = false) {
                            SheetHeader {
                                SheetTitle("Neue Nachricht")
                                SheetDescription("Von: Leitstelle")
                            }
                        }
                    }
                }
            }
            Muted("Das Seitenblatt bleibt in seinem Overlay-Container, hier einem Handy.", width = ElementSize.grow())
        }
    }
}

/**
 * Shows popovers on every side and a popover form.
 *
 * @param context the story context
 */
private fun ComponentScope.popoverStory(context: StoryContext) {
    storySection("Seiten") {
        Row(gap = 6) {
            OverlaySide.entries.forEach { side ->
                Popover(side) {
                    Button(side.slug(), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "popover_trigger_${side.slug()}")
                    PopoverContent { Muted("Popover ${side.slug()}") }
                }
            }
        }
    }
    storySection("Beispiel") {
        Popover(align = Alignment.START) {
            Button("Maße", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "popover_trigger")
            PopoverContent {
                PopoverHeader {
                    PopoverTitle(Component.text("Maße"))
                    PopoverDescription(Component.text("Lege die Maße der Ebene fest."))
                }
                Row(gap = 4, crossAlign = Alignment.CENTER) {
                    Label("Breite", width = ElementSize.fixed(40), forId = "popover_width")
                    Input(value = "100%", width = ElementSize.grow(), id = "popover_width", onChange = context.changed)
                }
                Button("Speichern", submitsInput = false, id = "popover_save", onClick = context.clicked)
            }
        }
    }
}

/**
 * Shows a hover card with a short profile.
 */
private fun ComponentScope.hoverCardStory() {
    storySection("Beispiel") {
        HoverCard {
            Button("@ammo", submitsInput = false, variant = ButtonVariant.LINK, id = "hover_trigger")
            HoverCardContent {
                Row(gap = 6) {
                    Avatar("AM")
                    Column(gap = 2, width = ElementSize.grow()) {
                        Large("@ammo")
                        Muted("Baut das Rollenspiel und seine Oberflächen.", width = ElementSize.grow())
                    }
                }
            }
        }
    }
}

/**
 * Shows tooltips on every side.
 *
 * @param context the story context
 */
private fun ComponentScope.tooltipStory(context: StoryContext) {
    storySection("Seiten") {
        Row(gap = 6) {
            OverlaySide.entries.forEach { side ->
                Tooltip("Tooltip ${side.slug()}", side) {
                    Button(side.slug(), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "tooltip_${side.slug()}", onClick = context.clicked)
                }
            }
        }
    }
    storySection("Beispiel") {
        Tooltip("Zur Akte hinzufügen") {
            Button(Component.empty(), submitsInput = false, icon = "plus", variant = ButtonVariant.OUTLINE, id = "tooltip_add", onClick = context.clicked)
        }
    }
}

/**
 * Shows a dropdown menu with labels, groups, shortcuts, a disabled, checkbox, radio and
 * destructive item, and a sub-menu.
 *
 * @param context the story context
 */
private fun ComponentScope.dropdownMenuStory(context: StoryContext) {
    storySection("Beispiel") {
        DropdownMenu(align = Alignment.START) {
            Button("Konto", submitsInput = false, icon = "user", variant = ButtonVariant.OUTLINE, id = "menu_trigger")
            DropdownMenuContent {
                DropdownMenuLabel("Mein Konto")
                DropdownMenuGroup {
                    DropdownMenuItem("Profil", icon = "user", shortcut = Component.text("⇧P"), id = "menu_profile", onClick = context.clicked)
                    DropdownMenuItem("Abrechnung", icon = "credit-card", id = "menu_billing", onClick = context.clicked)
                    DropdownMenuItem("Gesperrt", icon = "lock", enabled = false, id = "menu_locked")
                }
                DropdownMenuSeparator()
                DropdownMenuCheckboxItem("Statusleiste", checked = true, id = "menu_status", onToggle = context.clicked)
                DropdownMenuRadioGroup("bottom", onSelect = context.clicked, id = "menu_position") {
                    DropdownMenuRadioItem("Oben", "top")
                    DropdownMenuRadioItem("Unten", "bottom")
                }
                DropdownMenuSeparator()
                DropdownMenuSub {
                    DropdownMenuSubTrigger("Einladen", icon = "user-plus")
                    DropdownMenuSubContent {
                        DropdownMenuItem("Per Funk", icon = "radio", id = "menu_invite_radio", onClick = context.clicked)
                        DropdownMenuItem("Per Nachricht", icon = "message-square", id = "menu_invite_message", onClick = context.clicked)
                    }
                }
                DropdownMenuSeparator()
                DropdownMenuItem("Abmelden", icon = "log-out", destructive = true, id = "menu_logout", onClick = context.clicked)
            }
        }
    }
}

/**
 * Shows a context menu on an area, with shortcuts and a checkbox item.
 *
 * @param context the story context
 */
private fun ComponentScope.contextMenuStory(context: StoryContext) {
    storySection("Beispiel") {
        ContextMenu {
            Column(width = ElementSize.fixed(160), height = ElementSize.fixed(50), mainAlign = Alignment.CENTER, crossAlign = Alignment.CENTER) {
                Muted("Rechtsklick hier")
            }
            ContextMenuContent {
                ContextMenuItem("Zurück", shortcut = Component.text("Alt+←"), id = "context_back", onClick = context.clicked)
                ContextMenuItem("Neu laden", shortcut = Component.text("Strg+R"), id = "context_reload", onClick = context.clicked)
                ContextMenuItem("Gesperrt", enabled = false, id = "context_locked")
                ContextMenuSeparator()
                ContextMenuCheckboxItem("Lesezeichen zeigen", id = "context_bookmarks", onToggle = context.clicked)
            }
        }
    }
}

/**
 * Shows a menubar with three menus.
 *
 * @param context the story context
 */
private fun ComponentScope.menubarStory(context: StoryContext) {
    storySection("Beispiel") {
        Menubar {
            MenubarMenu {
                MenubarTrigger(Component.text("Datei"))
                MenubarContent {
                    MenubarItem("Neu", shortcut = Component.text("Strg+N"), id = "menubar_new", onClick = context.clicked)
                    MenubarItem("Öffnen", id = "menubar_open", onClick = context.clicked)
                    MenubarSeparator()
                    MenubarItem("Drucken", enabled = false, id = "menubar_print")
                }
            }
            MenubarMenu {
                MenubarTrigger(Component.text("Bearbeiten"))
                MenubarContent {
                    MenubarItem("Rückgängig", id = "menubar_undo", onClick = context.clicked)
                    MenubarItem("Wiederholen", id = "menubar_redo", onClick = context.clicked)
                }
            }
            MenubarMenu {
                MenubarTrigger(Component.text("Ansicht"))
                MenubarContent {
                    MenubarCheckboxItem("Vollbild", id = "menubar_fullscreen", onToggle = context.clicked)
                }
            }
        }
    }
}

/**
 * Shows a command menu with groups, keywords, shortcuts and a disabled item, and a command menu
 * in a dialog.
 *
 * @param context the story context
 */
private fun ComponentScope.commandStory(context: StoryContext) {
    storySection("Eingebettet") {
        Command {
            CommandInput(Component.text("Befehl oder Suche eingeben …"))
            CommandList {
                CommandEmpty(Component.text("Keine Ergebnisse."))
                CommandGroup(Component.text("Vorschläge")) {
                    CommandItem("Kalender", icon = "calendar", keywords = listOf("termin"), id = "command_calendar", onClick = context.clicked)
                    CommandItem("Personen suchen", icon = "search", id = "command_search", onClick = context.clicked)
                    CommandItem("Rechner", icon = "calculator", enabled = false, id = "command_calculator")
                }
                CommandSeparator()
                CommandGroup(Component.text("Einstellungen")) {
                    CommandItem("Profil", icon = "user", shortcut = Component.text("Strg+P"), id = "command_profile", onClick = context.clicked)
                }
            }
        }
    }
    storySection("Im Dialog") {
        Dialog {
            Button("Befehlsdialog öffnen", submitsInput = false, icon = "terminal", variant = ButtonVariant.OUTLINE, id = "command_dialog_trigger")
            DialogContent(showCloseButton = false) {
                Command {
                    CommandInput(Component.text("Befehl eingeben …"))
                    CommandList {
                        CommandEmpty(Component.text("Keine Ergebnisse."))
                        CommandItem("Hilfe", icon = "circle-help", id = "command_dialog_help", onClick = context.clicked)
                        CommandItem("Einstellungen", icon = "settings", id = "command_dialog_settings", onClick = context.clicked)
                    }
                }
            }
        }
    }
}

/**
 * Shows a toast to the viewing player if they are online.
 *
 * @param context the story context
 * @param toast the toast
 */
private fun showToast(context: StoryContext, toast: Toast) {
    val player = Bukkit.getPlayer(context.playerId) ?: return
    ToastService.show(player, toast)
}

/**
 * Shows buttons that send a toast of every type, with a description and a close button.
 *
 * @param context the story context
 */
private fun ComponentScope.toastStory(context: StoryContext) {
    storySection("Typen") {
        Row(gap = 4) {
            ToastType.entries.forEach { type ->
                Button(type.slug(), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "toast_${type.slug()}") {
                    showToast(context, Toast(Component.text("Toast ${type.slug()}"), type = type, durationMillis = Toast.DEFAULT_DURATION_MILLIS))
                }
            }
        }
    }
    storySection("Beispiel") {
        Button("Termin speichern", submitsInput = false, id = "toast_appointment") {
            showToast(context, Toast(Component.text("Termin angelegt"), Component.text("Freitag, 18:00 Uhr"), ToastType.SUCCESS, closeButton = true))
        }
    }
}

/**
 * Shows buttons that send toasts with actions, a loading toast that later succeeds, and a toast
 * that stays until it is closed.
 *
 * @param context the story context
 */
private fun ComponentScope.sonnerStory(context: StoryContext) {
    storySection("Aktionen") {
        Button("Funkspruch", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "sonner_action") {
            showToast(
                context,
                Toast(
                    Component.text("Eingehender Funkspruch"),
                    Component.text("Streife 12 ruft."),
                    action = ToastButton(Component.text("Annehmen")) { context.report("sonner_accept") },
                    cancel = ToastButton(Component.text("Ablehnen")) { context.report("sonner_decline") },
                    durationMillis = 10_000,
                ),
            )
        }
    }
    storySection("Laden und ersetzen") {
        Row(gap = 4) {
            Button("Hochladen", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "sonner_upload") {
                showToast(context, Toast(Component.text("Akte wird hochgeladen …"), type = ToastType.LOADING, id = SONNER_UPLOAD))
            }
            Button("Fertig", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "sonner_done") {
                showToast(context, Toast(Component.text("Akte hochgeladen"), type = ToastType.SUCCESS, id = SONNER_UPLOAD))
            }
        }
    }
    storySection("Bleibt stehen") {
        Button("Wartungshinweis", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "sonner_sticky") {
            showToast(context, Toast(Component.text("Wartungsarbeiten um 22:00 Uhr"), durationMillis = 0, closeButton = true, id = SONNER_STICKY))
        }
    }
}

/**
 * The id of the sonner story's upload toast, which the done button replaces.
 */
private const val SONNER_UPLOAD: String = "storybook-upload"

/**
 * The id of the sonner story's toast that stays until it is closed, so that a repeated click
 * replaces it instead of stacking another one.
 */
private const val SONNER_STICKY: String = "storybook-sticky"

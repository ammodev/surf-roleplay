package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.MenuContentElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuLabelElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuRadioItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
import net.kyori.adventure.text.Component

/** Adds the content of a dropdown menu: its entries on a bordered surface, as [MenuContent] does. */
fun ComponentScope.DropdownMenuContent(id: String? = null, children: ComponentScope.() -> Unit): MenuContentElement = MenuContent(id, children)

/** Adds a group of related dropdown menu entries, as [MenuGroup] does. */
fun ComponentScope.DropdownMenuGroup(id: String? = null, children: ComponentScope.() -> Unit): MenuGroupElement = MenuGroup(id, children)

/** Adds a heading in a dropdown menu, as [MenuLabel] does. */
fun ComponentScope.DropdownMenuLabel(text: Component, inset: Boolean = false, id: String? = null): MenuLabelElement = MenuLabel(text, inset, id)

/** Adds a plain heading in a dropdown menu, as [MenuLabel] does. */
fun ComponentScope.DropdownMenuLabel(text: String, inset: Boolean = false, id: String? = null): MenuLabelElement = MenuLabel(text, inset, id)

/** Adds an item of a dropdown menu that runs its handler and closes the menu when chosen, as [MenuItem] does. */
fun ComponentScope.DropdownMenuItem(text: Component, icon: String? = null, shortcut: Component? = null, destructive: Boolean = false, inset: Boolean = false, enabled: Boolean = true, id: String? = null, onClick: ButtonHandler? = null): MenuItemElement = MenuItem(text, icon, shortcut, destructive, inset, enabled, id, onClick)

/** Adds an item of a dropdown menu with a plain text that runs its handler and closes the menu when chosen, as [MenuItem] does. */
fun ComponentScope.DropdownMenuItem(text: String, icon: String? = null, shortcut: Component? = null, destructive: Boolean = false, inset: Boolean = false, enabled: Boolean = true, id: String? = null, onClick: ButtonHandler? = null): MenuItemElement = MenuItem(text, icon, shortcut, destructive, inset, enabled, id, onClick)

/** Adds an item of a dropdown menu with a check mark and returns the reference to whether it is checked, as [MenuCheckboxItem] does. */
fun ComponentScope.DropdownMenuCheckboxItem(text: Component, checked: Boolean = false, enabled: Boolean = true, id: String? = null, onToggle: ButtonHandler? = null): InputRef<Boolean> = MenuCheckboxItem(text, checked, enabled, id, onToggle)

/** Adds an item of a dropdown menu with a plain text and a check mark and returns the reference to whether it is checked, as [MenuCheckboxItem] does. */
fun ComponentScope.DropdownMenuCheckboxItem(text: String, checked: Boolean = false, enabled: Boolean = true, id: String? = null, onToggle: ButtonHandler? = null): InputRef<Boolean> = MenuCheckboxItem(text, checked, enabled, id, onToggle)

/** Adds a group of radio items in a dropdown menu and returns the reference to the value of the chosen item, as [MenuRadioGroup] does. */
fun ComponentScope.DropdownMenuRadioGroup(value: String = "", onSelect: ButtonHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<String?> = MenuRadioGroup(value, onSelect, id, children)

/** Adds an item of a radio group in a dropdown menu, as [MenuRadioItem] does. */
fun ComponentScope.DropdownMenuRadioItem(text: Component, value: String, enabled: Boolean = true, id: String? = null): MenuRadioItemElement = MenuRadioItem(text, value, enabled, id)

/** Adds an item with a plain text of a radio group in a dropdown menu, as [MenuRadioItem] does. */
fun ComponentScope.DropdownMenuRadioItem(text: String, value: String, enabled: Boolean = true, id: String? = null): MenuRadioItemElement = MenuRadioItem(text, value, enabled, id)

/** Adds a line between parts of a dropdown menu, as [MenuSeparator] does. */
fun ComponentScope.DropdownMenuSeparator(id: String? = null): MenuSeparatorElement = MenuSeparator(id)

/** Adds a sub-menu of a dropdown menu and returns the reference to whether it is open, as [MenuSub] does. */
fun ComponentScope.DropdownMenuSub(onChange: ChangeHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<Boolean> = MenuSub(onChange, id, children)

/** Adds the item of a dropdown menu that opens a sub-menu, as [MenuSubTrigger] does. */
fun ComponentScope.DropdownMenuSubTrigger(text: Component, icon: String? = null, inset: Boolean = false, enabled: Boolean = true, id: String? = null): MenuSubTriggerElement = MenuSubTrigger(text, icon, inset, enabled, id)

/** Adds the item of a dropdown menu with a plain text that opens a sub-menu, as [MenuSubTrigger] does. */
fun ComponentScope.DropdownMenuSubTrigger(text: String, icon: String? = null, inset: Boolean = false, enabled: Boolean = true, id: String? = null): MenuSubTriggerElement = MenuSubTrigger(text, icon, inset, enabled, id)

/** Adds the content of a sub-menu of a dropdown menu, as [MenuContent] does. */
fun ComponentScope.DropdownMenuSubContent(id: String? = null, children: ComponentScope.() -> Unit): MenuContentElement = MenuContent(id, children)

/** Adds the content of a context menu: its entries on a bordered surface, as [MenuContent] does. */
fun ComponentScope.ContextMenuContent(id: String? = null, children: ComponentScope.() -> Unit): MenuContentElement = MenuContent(id, children)

/** Adds a group of related context menu entries, as [MenuGroup] does. */
fun ComponentScope.ContextMenuGroup(id: String? = null, children: ComponentScope.() -> Unit): MenuGroupElement = MenuGroup(id, children)

/** Adds a heading in a context menu, as [MenuLabel] does. */
fun ComponentScope.ContextMenuLabel(text: Component, inset: Boolean = false, id: String? = null): MenuLabelElement = MenuLabel(text, inset, id)

/** Adds a plain heading in a context menu, as [MenuLabel] does. */
fun ComponentScope.ContextMenuLabel(text: String, inset: Boolean = false, id: String? = null): MenuLabelElement = MenuLabel(text, inset, id)

/** Adds an item of a context menu that runs its handler and closes the menu when chosen, as [MenuItem] does. */
fun ComponentScope.ContextMenuItem(text: Component, icon: String? = null, shortcut: Component? = null, destructive: Boolean = false, inset: Boolean = false, enabled: Boolean = true, id: String? = null, onClick: ButtonHandler? = null): MenuItemElement = MenuItem(text, icon, shortcut, destructive, inset, enabled, id, onClick)

/** Adds an item of a context menu with a plain text that runs its handler and closes the menu when chosen, as [MenuItem] does. */
fun ComponentScope.ContextMenuItem(text: String, icon: String? = null, shortcut: Component? = null, destructive: Boolean = false, inset: Boolean = false, enabled: Boolean = true, id: String? = null, onClick: ButtonHandler? = null): MenuItemElement = MenuItem(text, icon, shortcut, destructive, inset, enabled, id, onClick)

/** Adds an item of a context menu with a check mark and returns the reference to whether it is checked, as [MenuCheckboxItem] does. */
fun ComponentScope.ContextMenuCheckboxItem(text: Component, checked: Boolean = false, enabled: Boolean = true, id: String? = null, onToggle: ButtonHandler? = null): InputRef<Boolean> = MenuCheckboxItem(text, checked, enabled, id, onToggle)

/** Adds an item of a context menu with a plain text and a check mark and returns the reference to whether it is checked, as [MenuCheckboxItem] does. */
fun ComponentScope.ContextMenuCheckboxItem(text: String, checked: Boolean = false, enabled: Boolean = true, id: String? = null, onToggle: ButtonHandler? = null): InputRef<Boolean> = MenuCheckboxItem(text, checked, enabled, id, onToggle)

/** Adds a group of radio items in a context menu and returns the reference to the value of the chosen item, as [MenuRadioGroup] does. */
fun ComponentScope.ContextMenuRadioGroup(value: String = "", onSelect: ButtonHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<String?> = MenuRadioGroup(value, onSelect, id, children)

/** Adds an item of a radio group in a context menu, as [MenuRadioItem] does. */
fun ComponentScope.ContextMenuRadioItem(text: Component, value: String, enabled: Boolean = true, id: String? = null): MenuRadioItemElement = MenuRadioItem(text, value, enabled, id)

/** Adds an item with a plain text of a radio group in a context menu, as [MenuRadioItem] does. */
fun ComponentScope.ContextMenuRadioItem(text: String, value: String, enabled: Boolean = true, id: String? = null): MenuRadioItemElement = MenuRadioItem(text, value, enabled, id)

/** Adds a line between parts of a context menu, as [MenuSeparator] does. */
fun ComponentScope.ContextMenuSeparator(id: String? = null): MenuSeparatorElement = MenuSeparator(id)

/** Adds a sub-menu of a context menu and returns the reference to whether it is open, as [MenuSub] does. */
fun ComponentScope.ContextMenuSub(onChange: ChangeHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<Boolean> = MenuSub(onChange, id, children)

/** Adds the item of a context menu that opens a sub-menu, as [MenuSubTrigger] does. */
fun ComponentScope.ContextMenuSubTrigger(text: Component, icon: String? = null, inset: Boolean = false, enabled: Boolean = true, id: String? = null): MenuSubTriggerElement = MenuSubTrigger(text, icon, inset, enabled, id)

/** Adds the item of a context menu with a plain text that opens a sub-menu, as [MenuSubTrigger] does. */
fun ComponentScope.ContextMenuSubTrigger(text: String, icon: String? = null, inset: Boolean = false, enabled: Boolean = true, id: String? = null): MenuSubTriggerElement = MenuSubTrigger(text, icon, inset, enabled, id)

/** Adds the content of a sub-menu of a context menu, as [MenuContent] does. */
fun ComponentScope.ContextMenuSubContent(id: String? = null, children: ComponentScope.() -> Unit): MenuContentElement = MenuContent(id, children)

/** Adds the content of a menubar menu: its entries on a bordered surface, as [MenuContent] does. */
fun ComponentScope.MenubarContent(id: String? = null, children: ComponentScope.() -> Unit): MenuContentElement = MenuContent(id, children)

/** Adds a group of related menubar menu entries, as [MenuGroup] does. */
fun ComponentScope.MenubarGroup(id: String? = null, children: ComponentScope.() -> Unit): MenuGroupElement = MenuGroup(id, children)

/** Adds a heading in a menubar menu, as [MenuLabel] does. */
fun ComponentScope.MenubarLabel(text: Component, inset: Boolean = false, id: String? = null): MenuLabelElement = MenuLabel(text, inset, id)

/** Adds a plain heading in a menubar menu, as [MenuLabel] does. */
fun ComponentScope.MenubarLabel(text: String, inset: Boolean = false, id: String? = null): MenuLabelElement = MenuLabel(text, inset, id)

/** Adds an item of a menubar menu that runs its handler and closes the menu when chosen, as [MenuItem] does. */
fun ComponentScope.MenubarItem(text: Component, icon: String? = null, shortcut: Component? = null, destructive: Boolean = false, inset: Boolean = false, enabled: Boolean = true, id: String? = null, onClick: ButtonHandler? = null): MenuItemElement = MenuItem(text, icon, shortcut, destructive, inset, enabled, id, onClick)

/** Adds an item of a menubar menu with a plain text that runs its handler and closes the menu when chosen, as [MenuItem] does. */
fun ComponentScope.MenubarItem(text: String, icon: String? = null, shortcut: Component? = null, destructive: Boolean = false, inset: Boolean = false, enabled: Boolean = true, id: String? = null, onClick: ButtonHandler? = null): MenuItemElement = MenuItem(text, icon, shortcut, destructive, inset, enabled, id, onClick)

/** Adds an item of a menubar menu with a check mark and returns the reference to whether it is checked, as [MenuCheckboxItem] does. */
fun ComponentScope.MenubarCheckboxItem(text: Component, checked: Boolean = false, enabled: Boolean = true, id: String? = null, onToggle: ButtonHandler? = null): InputRef<Boolean> = MenuCheckboxItem(text, checked, enabled, id, onToggle)

/** Adds an item of a menubar menu with a plain text and a check mark and returns the reference to whether it is checked, as [MenuCheckboxItem] does. */
fun ComponentScope.MenubarCheckboxItem(text: String, checked: Boolean = false, enabled: Boolean = true, id: String? = null, onToggle: ButtonHandler? = null): InputRef<Boolean> = MenuCheckboxItem(text, checked, enabled, id, onToggle)

/** Adds a group of radio items in a menubar menu and returns the reference to the value of the chosen item, as [MenuRadioGroup] does. */
fun ComponentScope.MenubarRadioGroup(value: String = "", onSelect: ButtonHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<String?> = MenuRadioGroup(value, onSelect, id, children)

/** Adds an item of a radio group in a menubar menu, as [MenuRadioItem] does. */
fun ComponentScope.MenubarRadioItem(text: Component, value: String, enabled: Boolean = true, id: String? = null): MenuRadioItemElement = MenuRadioItem(text, value, enabled, id)

/** Adds an item with a plain text of a radio group in a menubar menu, as [MenuRadioItem] does. */
fun ComponentScope.MenubarRadioItem(text: String, value: String, enabled: Boolean = true, id: String? = null): MenuRadioItemElement = MenuRadioItem(text, value, enabled, id)

/** Adds a line between parts of a menubar menu, as [MenuSeparator] does. */
fun ComponentScope.MenubarSeparator(id: String? = null): MenuSeparatorElement = MenuSeparator(id)

/** Adds a sub-menu of a menubar menu and returns the reference to whether it is open, as [MenuSub] does. */
fun ComponentScope.MenubarSub(onChange: ChangeHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<Boolean> = MenuSub(onChange, id, children)

/** Adds the item of a menubar menu that opens a sub-menu, as [MenuSubTrigger] does. */
fun ComponentScope.MenubarSubTrigger(text: Component, icon: String? = null, inset: Boolean = false, enabled: Boolean = true, id: String? = null): MenuSubTriggerElement = MenuSubTrigger(text, icon, inset, enabled, id)

/** Adds the item of a menubar menu with a plain text that opens a sub-menu, as [MenuSubTrigger] does. */
fun ComponentScope.MenubarSubTrigger(text: String, icon: String? = null, inset: Boolean = false, enabled: Boolean = true, id: String? = null): MenuSubTriggerElement = MenuSubTrigger(text, icon, inset, enabled, id)

/** Adds the content of a sub-menu of a menubar menu, as [MenuContent] does. */
fun ComponentScope.MenubarSubContent(id: String? = null, children: ComponentScope.() -> Unit): MenuContentElement = MenuContent(id, children)

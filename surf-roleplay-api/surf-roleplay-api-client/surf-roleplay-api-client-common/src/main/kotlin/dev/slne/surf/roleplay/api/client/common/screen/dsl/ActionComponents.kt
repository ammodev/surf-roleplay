package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.ToggleElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupChoice
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleSize
import dev.slne.surf.roleplay.api.client.common.screen.ToggleVariant
import net.kyori.adventure.text.Component

/**
 * Adds a button.
 *
 * @param text the caption
 * @param enabled whether the button can be clicked
 * @param width how wide the button is laid out
 * @param height how tall the button is laid out
 * @param submitsInput whether a click requires every input of the screen to be valid
 * @param icon the Lucide name of an icon drawn before the caption, or `null` for none
 * @param variant the look of the button
 * @param size the size of the button
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run on a validated click, or `null` for none
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Button(
    text: Component,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    submitsInput: Boolean = true,
    icon: String? = null,
    variant: ButtonVariant = ButtonVariant.DEFAULT,
    size: ButtonSize = ButtonSize.DEFAULT,
    id: String? = null,
    onClick: ButtonHandler? = null,
): ButtonElement {
    val elementId = nextId(id)
    return add(ButtonElement(elementId, text, enabled, bindButton(elementId, onClick), width, height, submitsInput, icon, variant, size))
}

/**
 * Adds a button with a plain caption.
 *
 * @param text the caption
 * @param enabled whether the button can be clicked
 * @param width how wide the button is laid out
 * @param height how tall the button is laid out
 * @param submitsInput whether a click requires every input of the screen to be valid
 * @param icon the Lucide name of an icon drawn before the caption, or `null` for none
 * @param variant the look of the button
 * @param size the size of the button
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run on a validated click, or `null` for none
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Button(
    text: String,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    submitsInput: Boolean = true,
    icon: String? = null,
    variant: ButtonVariant = ButtonVariant.DEFAULT,
    size: ButtonSize = ButtonSize.DEFAULT,
    id: String? = null,
    onClick: ButtonHandler? = null,
): ButtonElement = Button(Component.text(text), enabled, width, height, submitsInput, icon, variant, size, id, onClick)

/**
 * Adds a group of buttons joined into one control.
 *
 * @param orientation whether the group runs horizontally or vertically
 * @param width how wide the group is laid out
 * @param height how tall the group is laid out
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the group's buttons, texts and separators
 * @return the group
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ButtonGroup(
    orientation: Orientation = Orientation.HORIZONTAL,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): ButtonGroupElement {
    val elementId = nextId(id)
    return add(ButtonGroupElement(elementId, this.children(children), orientation, width, height))
}

/**
 * Adds a text part to a button group, drawn like a muted button.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param id the id of the text, or `null` for a generated one
 * @return the text part
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ButtonGroupText(text: Component, icon: String? = null, id: String? = null): ButtonGroupTextElement =
    add(ButtonGroupTextElement(nextId(id), text, icon))

/**
 * Adds a plain text part to a button group, drawn like a muted button.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param id the id of the text, or `null` for a generated one
 * @return the text part
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ButtonGroupText(text: String, icon: String? = null, id: String? = null): ButtonGroupTextElement =
    ButtonGroupText(Component.text(text), icon, id)

/**
 * Adds a line between the parts of a button group.
 *
 * @param id the id of the separator, or `null` for a generated one
 * @return the separator
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ButtonGroupSeparator(id: String? = null): ButtonGroupSeparatorElement = add(ButtonGroupSeparatorElement(nextId(id)))

/**
 * Adds a two-state button. Pressing it switches its state and runs its handler; other inputs do
 * not have to be valid for a press.
 *
 * @param text the caption
 * @param icon the Lucide name of an icon drawn before the caption, or `null` for none
 * @param pressed whether the toggle is initially on
 * @param variant the look of the toggle
 * @param size the size of the toggle
 * @param enabled whether the toggle can be pressed
 * @param id the id of the toggle, or `null` for a generated one
 * @param onToggle the handler run on every validated press, or `null` for none
 * @return the reference to the toggle's state
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Toggle(
    text: Component = Component.empty(),
    icon: String? = null,
    pressed: Boolean = false,
    variant: ToggleVariant = ToggleVariant.DEFAULT,
    size: ToggleSize = ToggleSize.DEFAULT,
    enabled: Boolean = true,
    id: String? = null,
    onToggle: ButtonHandler? = null,
): InputRef<Boolean> {
    val elementId = nextId(id)
    add(ToggleElement(elementId, text, icon, pressed, variant, size, enabled, bindButton(elementId, onToggle)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds a two-state button with a plain caption. Pressing it switches its state and runs its
 * handler; other inputs do not have to be valid for a press.
 *
 * @param text the caption
 * @param icon the Lucide name of an icon drawn before the caption, or `null` for none
 * @param pressed whether the toggle is initially on
 * @param variant the look of the toggle
 * @param size the size of the toggle
 * @param enabled whether the toggle can be pressed
 * @param id the id of the toggle, or `null` for a generated one
 * @param onToggle the handler run on every validated press, or `null` for none
 * @return the reference to the toggle's state
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Toggle(
    text: String,
    icon: String? = null,
    pressed: Boolean = false,
    variant: ToggleVariant = ToggleVariant.DEFAULT,
    size: ToggleSize = ToggleSize.DEFAULT,
    enabled: Boolean = true,
    id: String? = null,
    onToggle: ButtonHandler? = null,
): InputRef<Boolean> = Toggle(Component.text(text), icon, pressed, variant, size, enabled, id, onToggle)

/**
 * Adds a group of toggles joined into one input. Its value is the values of the items that are
 * on, in item order.
 *
 * @param items the items, in order
 * @param multiple whether several items can be on at once; otherwise at most one is
 * @param selected the values of the items that are initially on
 * @param variant the look of the items
 * @param size the size of the items
 * @param spacing the space between items in GUI pixels; zero joins them
 * @param orientation whether the items run horizontally or vertically
 * @param enabled whether the player can switch items
 * @param required whether having no item on is invalid
 * @param id the id of the group, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to the values of the items that are on
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ToggleGroup(
    items: List<ToggleGroupChoice>,
    multiple: Boolean = false,
    selected: List<String> = emptyList(),
    variant: ToggleVariant = ToggleVariant.DEFAULT,
    size: ToggleSize = ToggleSize.DEFAULT,
    spacing: Int = 0,
    orientation: Orientation = Orientation.HORIZONTAL,
    enabled: Boolean = true,
    required: Boolean = false,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<List<String>> {
    val elementId = nextId(id)
    add(
        ToggleGroupElement(
            elementId, items, multiple, selected, variant, size, spacing, orientation, enabled, required,
            bindChange(elementId, onChange),
        ),
    )
    return InputRef(elementId, InputParsers.list)
}

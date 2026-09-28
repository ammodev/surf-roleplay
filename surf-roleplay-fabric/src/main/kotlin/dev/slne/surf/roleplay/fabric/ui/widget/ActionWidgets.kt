package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.Corners
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeTokens
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ButtonSize
import dev.slne.surf.roleplay.protocol.screen.ButtonVariant
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.ToggleGroupItem
import dev.slne.surf.roleplay.protocol.screen.ToggleSize
import dev.slne.surf.roleplay.protocol.screen.ToggleVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * The sizes and colours of buttons and toggles, following shadcn's variants.
 */
object ButtonStyle {

    /**
     * The colours of one button state.
     *
     * @property background the fill, or `null` for none
     * @property foreground the colour of text and icons
     * @property border the border colour, or `null` for none
     * @property underline whether the caption is underlined
     */
    data class Colors(val background: Int?, val foreground: Int, val border: Int?, val underline: Boolean = false)

    /**
     * Returns the height of a button size.
     *
     * @param size the size
     * @return the height in GUI pixels
     */
    fun height(size: ButtonSize): Int = when (size) {
        ButtonSize.XS, ButtonSize.ICON_XS -> 14
        ButtonSize.SM, ButtonSize.ICON_SM -> 16
        ButtonSize.DEFAULT, ButtonSize.ICON -> 20
        ButtonSize.LG, ButtonSize.ICON_LG -> 24
    }

    /**
     * Returns whether a button size shows only the icon.
     *
     * @param size the size
     * @return whether it is an icon size
     */
    fun isIcon(size: ButtonSize): Boolean = size in setOf(ButtonSize.ICON, ButtonSize.ICON_XS, ButtonSize.ICON_SM, ButtonSize.ICON_LG)

    /**
     * Returns the horizontal padding of a button size.
     *
     * @param size the size
     * @return the padding on each side in GUI pixels
     */
    fun padding(size: ButtonSize): Int = when (size) {
        ButtonSize.XS, ButtonSize.ICON_XS -> 4
        ButtonSize.SM, ButtonSize.ICON_SM -> 6
        ButtonSize.DEFAULT, ButtonSize.ICON -> 8
        ButtonSize.LG, ButtonSize.ICON_LG -> 12
    }

    /**
     * Computes the content size of a button.
     *
     * @param size the button size
     * @param icon the icon name, or `null`
     * @param textWidth the width of the caption
     * @return a square for icon sizes, otherwise the caption and icon with padding
     */
    fun size(size: ButtonSize, icon: String?, textWidth: Int): Size {
        val height = height(size)
        if (isIcon(size)) return Size(height, height)
        return Size(iconSpace(icon, textWidth) + textWidth + 2 * padding(size), height)
    }

    /**
     * Returns the colours of a button variant.
     *
     * @param tokens the theme tokens
     * @param variant the variant
     * @param hovered whether the mouse is over the button
     * @return the colours
     */
    fun colors(tokens: ThemeTokens, variant: ButtonVariant, hovered: Boolean): Colors = when (variant) {
        ButtonVariant.DEFAULT -> Colors(hover(tokens.primary, tokens, hovered), tokens.primaryForeground, null)
        ButtonVariant.DESTRUCTIVE -> Colors(hover(tokens.destructive, tokens, hovered), tokens.destructiveForeground, null)
        ButtonVariant.OUTLINE -> Colors(
            if (hovered) tokens.accent else translucentInput(tokens),
            if (hovered) tokens.accentForeground else tokens.foreground,
            tokens.input,
        )
        ButtonVariant.SECONDARY -> Colors(hover(tokens.secondary, tokens, hovered, SECONDARY_HOVER), tokens.secondaryForeground, null)
        ButtonVariant.GHOST -> Colors(if (hovered) tokens.accent else null, if (hovered) tokens.accentForeground else tokens.foreground, null)
        ButtonVariant.LINK -> Colors(null, tokens.primary, null, underline = hovered)
    }

    /**
     * Returns the colours of a toggle.
     *
     * @param tokens the theme tokens
     * @param variant the variant
     * @param hovered whether the mouse is over the toggle
     * @param pressed whether the toggle is on
     * @return the colours
     */
    fun toggleColors(tokens: ThemeTokens, variant: ToggleVariant, hovered: Boolean, pressed: Boolean): Colors {
        val background = when {
            pressed -> tokens.accent
            hovered -> tokens.muted
            variant == ToggleVariant.OUTLINE -> translucentInput(tokens)
            else -> null
        }
        val foreground = when {
            pressed -> tokens.accentForeground
            hovered -> tokens.mutedForeground
            else -> tokens.foreground
        }
        return Colors(background, foreground, if (variant == ToggleVariant.OUTLINE) tokens.input else null)
    }

    /**
     * Returns the height of a toggle size.
     *
     * @param size the size
     * @return the height in GUI pixels
     */
    fun toggleHeight(size: ToggleSize): Int = when (size) {
        ToggleSize.SM -> 16
        ToggleSize.DEFAULT -> 20
        ToggleSize.LG -> 24
    }

    /**
     * Returns the horizontal padding of a toggle size.
     *
     * @param size the size
     * @return the padding on each side in GUI pixels
     */
    fun togglePadding(size: ToggleSize): Int = when (size) {
        ToggleSize.SM -> 4
        ToggleSize.DEFAULT -> 6
        ToggleSize.LG -> 8
    }

    /**
     * Computes the content size of a toggle or toggle group item: its caption and icon with
     * padding, at least square.
     *
     * @param size the toggle size
     * @param icon the icon name, or `null`
     * @param textWidth the width of the caption
     * @return the size
     */
    fun toggleSize(size: ToggleSize, icon: String?, textWidth: Int): Size {
        val height = toggleHeight(size)
        return Size(maxOf(height, iconSpace(icon, textWidth) + textWidth + 2 * togglePadding(size)), height)
    }

    /**
     * Draws a button-like control: its fill, border, icon and caption.
     *
     * @param ui the graphics to draw with
     * @param area the control's area
     * @param corners the rounded corners
     * @param colors the colours of the current state
     * @param text the caption as component JSON
     * @param icon the icon name, or `null`
     * @param enabled whether the control is enabled, which dims it when not
     * @param iconOnly whether only the icon is drawn
     */
    fun drawControl(ui: UiGraphics, area: Rect, corners: Corners, colors: Colors, text: String, icon: String?, enabled: Boolean, iconOnly: Boolean) {
        val fade: (Int) -> Int = { if (enabled) it else ui.disabled(it) }
        colors.background?.let { ui.fillRounded(area, fade(it), corners = corners) }
        colors.border?.let { ui.borderRounded(area, fade(it), corners = corners) }
        val color = fade(colors.foreground)
        ui.clipped(area) {
            if (iconOnly) {
                val side = UiMetrics.INLINE_ICON
                icon?.let { ui.icon(it, Rect(area.x + (area.width - side) / 2, area.y + (area.height - side) / 2, side, side), color) }
                return@clipped
            }
            val textWidth = ui.width(text)
            val groupWidth = iconSpace(icon, textWidth) + textWidth
            val textX = drawLeadingIcon(ui, icon, area.x + (area.width - groupWidth) / 2, area, color, textWidth)
            val textY = area.y + (area.height - ui.lineHeight + 1) / 2
            ui.text(text, textX, textY, color)
            if (colors.underline) ui.fill(Rect(textX, textY + ui.lineHeight - 1, textWidth, 1), color)
        }
    }

    /**
     * Draws a button in its variant and size.
     *
     * @param ui the graphics to draw with
     * @param area the button's area
     * @param corners the rounded corners
     * @param variant the variant
     * @param size the size
     * @param text the caption as component JSON
     * @param icon the icon name, or `null`
     * @param enabled whether the button is enabled
     * @param hovered whether the mouse is over the button
     * @param pressed unused for buttons
     */
    fun draw(
        ui: UiGraphics,
        area: Rect,
        corners: Corners,
        variant: ButtonVariant,
        size: ButtonSize,
        text: String,
        icon: String?,
        enabled: Boolean,
        hovered: Boolean,
        pressed: Boolean,
    ) = drawControl(ui, area, corners, colors(ui.tokens, variant, enabled && hovered), text, icon, enabled, isIcon(size))

    /**
     * Returns a fill colour, blended towards the background when hovered.
     *
     * @param color the fill
     * @param tokens the theme tokens
     * @param hovered whether the mouse is over the control
     * @param amount how far to blend when hovered
     * @return the fill to draw
     */
    private fun hover(color: Int, tokens: ThemeTokens, hovered: Boolean, amount: Float = PRIMARY_HOVER): Int =
        if (hovered) ThemeColors.blend(color, tokens.background, amount) else color

    /**
     * Returns the translucent input fill used behind outline controls.
     *
     * @param tokens the theme tokens
     * @return the fill
     */
    private fun translucentInput(tokens: ThemeTokens): Int = ThemeColors.withAlpha(tokens.input, ((tokens.input ushr 24) / 255f) * 0.3f)

    /**
     * How far a hovered filled button is blended towards the background.
     */
    private const val PRIMARY_HOVER: Float = 0.1f

    /**
     * How far a hovered secondary button is blended towards the background.
     */
    private const val SECONDARY_HOVER: Float = 0.2f
}

/**
 * A group of buttons joined into one control: the parts touch, and only the group's outer corners
 * are rounded.
 *
 * @param id the id of the widget
 * @property orientation whether the group runs horizontally or vertically
 */
class ButtonGroupWidget(id: String, val orientation: Orientation) :
    ContainerWidget(id, if (orientation == Orientation.HORIZONTAL) Axis.HORIZONTAL else Axis.VERTICAL) {

    init {
        crossAlign = Align.STRETCH
    }

    /**
     * Sets the corner mask of every part: the first and last part round their outer corners, and
     * parts in between round none.
     */
    fun assignCorners() {
        val parts = childList
        parts.forEachIndexed { index, part ->
            part.corners = when {
                parts.size == 1 -> Corners.ALL
                index == 0 -> if (orientation == Orientation.HORIZONTAL) Corners.LEFT else Corners.TOP
                index == parts.lastIndex -> if (orientation == Orientation.HORIZONTAL) Corners.RIGHT else Corners.BOTTOM
                else -> Corners.NONE
            }
        }
    }

    /**
     * Copies the computed bounds into the group and its parts and assigns the parts' corners.
     */
    override fun applyLayout() {
        super.applyLayout()
        assignCorners()
    }
}

/**
 * A text part of a button group, drawn like a muted button.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 */
class ButtonGroupTextWidget(id: String, var text: String, var icon: String?) : Widget(id) {

    /**
     * Returns the size of the text and icon with padding, at the regular button height.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        Size(iconSpace(icon, measurer.width(text)) + measurer.width(text) + 2 * UiMetrics.WIDGET_PADDING, UiMetrics.WIDGET_HEIGHT)

    /**
     * Draws the text on a muted fill with a border.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        ButtonStyle.drawControl(ui, bounds, corners, ButtonStyle.Colors(tokens.muted, tokens.foreground, tokens.input), text, icon, enabled = true, iconOnly = false)
    }

    /**
     * Replaces the text.
     *
     * @param json the new text as component JSON
     */
    override fun applyText(json: String) {
        text = json
    }
}

/**
 * A line between the parts of a button group, stretched across the group.
 *
 * @param id the id of the widget
 */
class ButtonGroupSeparatorWidget(id: String) : Widget(id) {

    /**
     * Returns a one-pixel size, which the group stretches across its cross axis.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(1, 1)

    /**
     * Draws the line in the input colour.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.fill(bounds, ui.tokens.input)
    }
}

/**
 * A two-state button that triggers an action whenever it is pressed.
 *
 * @param id the id of the widget
 * @property text the caption as component JSON
 * @property icon the Lucide name of the leading icon, or `null` for none
 * @property pressed whether the toggle is on
 * @property variant the look of the toggle
 * @property size the size of the toggle
 */
class ToggleWidget(
    id: String,
    var text: String,
    var icon: String?,
    var pressed: Boolean,
    val variant: ToggleVariant,
    val size: ToggleSize,
) : Widget(id) {

    /**
     * Whether the toggle can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * `true` if the toggle is on, `false` otherwise.
     */
    override val inputValue: String get() = pressed.toString()

    /**
     * Returns the size of the caption and icon with padding, at least square.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = ButtonStyle.toggleSize(size, icon, measurer.width(text))

    /**
     * Draws the toggle in its state.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val colors = ButtonStyle.toggleColors(ui.tokens, variant, enabled && isOver(mouseX, mouseY), pressed)
        ButtonStyle.drawControl(ui, bounds, corners, colors, text, icon, enabled, iconOnly = text.isEmpty())
    }

    /**
     * Switches the toggle, marks it as changed and triggers its action.
     *
     * @param context the screen showing the widget
     */
    fun press(context: UiContext) {
        pressed = !pressed
        touched = true
        context.actionTriggered(this, submitsInput = false)
    }

    /**
     * Presses an enabled toggle on a left click and focuses it.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this toggle
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            press(context)
        }
        return true
    }

    /**
     * Presses the toggle on Enter or Space.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled || !isActivation(event)) return false
        press(context)
        return true
    }

    /**
     * Sets the state from `true` or `false`.
     *
     * @param value the new state
     */
    override fun applyValue(value: String) {
        pressed = value == "true"
    }

    /**
     * Replaces the caption.
     *
     * @param json the new caption as component JSON
     */
    override fun applyText(json: String) {
        text = json
    }
}

/**
 * A group of toggles joined into one input. Its value is the values of the items that are on,
 * joined by commas in item order.
 *
 * @param id the id of the widget
 * @property items the items, in order
 * @param selected the values of the items that are initially on
 * @property multiple whether several items can be on at once
 * @property variant the look of the items
 * @property size the size of the items
 * @property spacing the space between items; zero joins them
 * @property orientation whether the items run horizontally or vertically
 * @property required whether having no item on is invalid
 */
class ToggleGroupWidget(
    id: String,
    val items: List<ToggleGroupItem>,
    selected: List<String>,
    val multiple: Boolean,
    val variant: ToggleVariant,
    val size: ToggleSize,
    val spacing: Int,
    val orientation: Orientation,
    val required: Boolean,
) : Widget(id) {

    /**
     * The values of the items that are on.
     */
    private val on: MutableSet<String> = selected.toMutableSet()

    /**
     * The item the keyboard acts on.
     */
    var highlighted: Int = items.indexOfFirst { it.value in on }.coerceAtLeast(0)
        private set

    /**
     * The content sizes of the items, measured by the last layout.
     */
    private var itemSizes: List<Size> = emptyList()

    /**
     * Whether the group can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * The values of the items that are on, joined by commas in item order.
     */
    override val inputValue: String get() = items.filter { it.value in on }.joinToString(",") { it.value }

    /**
     * Whether the group shows itself as invalid: once touched while required and empty.
     */
    override val showsInvalid: Boolean get() = touched && required && on.isEmpty()

    /**
     * Measures the items and returns their total size along the group's orientation.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        itemSizes = items.map { ButtonStyle.toggleSize(size, it.icon, measurer.width(it.text)) }
        val gaps = spacing * (items.size - 1).coerceAtLeast(0)
        return if (orientation == Orientation.HORIZONTAL) {
            Size(itemSizes.sumOf { it.width } + gaps, itemSizes.maxOfOrNull { it.height } ?: 0)
        } else {
            Size(itemSizes.maxOfOrNull { it.width } ?: 0, itemSizes.sumOf { it.height } + gaps)
        }
    }

    /**
     * Returns the area of every item within the group's bounds.
     *
     * @return the item areas, in order
     */
    private fun itemAreas(): List<Rect> {
        var cursor = if (orientation == Orientation.HORIZONTAL) bounds.x else bounds.y
        return items.indices.map { index ->
            val itemSize = itemSizes.getOrElse(index) { Size.ZERO }
            val area = if (orientation == Orientation.HORIZONTAL) {
                Rect(cursor, bounds.y, itemSize.width, bounds.height)
            } else {
                Rect(bounds.x, cursor, bounds.width, itemSize.height)
            }
            cursor += (if (orientation == Orientation.HORIZONTAL) itemSize.width else itemSize.height) + spacing
            area
        }
    }

    /**
     * Returns the rounded corners of an item: all when spaced, only the outer ones when joined.
     *
     * @param index the item index
     * @return the corners
     */
    private fun itemCorners(index: Int): Corners = when {
        spacing > 0 || items.size == 1 -> Corners.ALL
        index == 0 -> if (orientation == Orientation.HORIZONTAL) Corners.LEFT else Corners.TOP
        index == items.lastIndex -> if (orientation == Orientation.HORIZONTAL) Corners.RIGHT else Corners.BOTTOM
        else -> Corners.NONE
    }

    /**
     * Draws every item in its state, with the keyboard highlight on the focused group.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        itemAreas().forEachIndexed { index, area ->
            val item = items[index]
            val active = enabled && item.enabled
            val colors = ButtonStyle.toggleColors(ui.tokens, variant, active && area.contains(mouseX.toDouble(), mouseY.toDouble()), item.value in on)
            ButtonStyle.drawControl(ui, area, itemCorners(index), colors, item.text, item.icon, active, iconOnly = item.text.isEmpty())
            if (context.focusedWidget === this && index == highlighted) ui.borderRounded(area, ui.tokens.ring, corners = itemCorners(index))
        }
        if (showsInvalid) ui.borderRounded(bounds, ui.tokens.destructive)
    }

    /**
     * Switches an item: in a multiple group it turns on or off, in a single group it becomes the
     * only item that is on, or turns off if it already was. Disabled items do not switch.
     *
     * @param index the item index
     * @param context the screen showing the widget
     */
    fun switch(index: Int, context: UiContext) {
        val item = items.getOrNull(index) ?: return
        if (!enabled || !item.enabled) return
        highlighted = index
        if (multiple) {
            if (!on.remove(item.value)) on += item.value
        } else {
            val wasOn = item.value in on
            on.clear()
            if (!wasOn) on += item.value
        }
        markChanged(context, immediate = true)
    }

    /**
     * Moves the keyboard highlight by a number of items, stopping at the ends.
     *
     * @param delta the number of items to move; negative moves back
     */
    fun moveHighlight(delta: Int) {
        highlighted = (highlighted + delta).coerceIn(0, (items.size - 1).coerceAtLeast(0))
    }

    /**
     * Switches the item under a left click and focuses the group.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this group
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (!enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true
        context.focus(this)
        val index = itemAreas().indexOfFirst { it.contains(x, y) }
        if (index >= 0) switch(index, context)
        return true
    }

    /**
     * Moves the highlight with the arrow keys and switches the highlighted item on Enter or Space.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled) return false
        when (event.key()) {
            GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_UP -> moveHighlight(-1)
            GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_DOWN -> moveHighlight(1)
            else -> if (isActivation(event)) switch(highlighted, context) else return false
        }
        return true
    }

    /**
     * Sets the items that are on from a comma-separated list of values.
     *
     * @param value the list of values
     */
    override fun applyValue(value: String) {
        on.clear()
        on += value.split(',').map { it.trim() }.filter { v -> v.isNotEmpty() && items.any { it.value == v } }
    }
}

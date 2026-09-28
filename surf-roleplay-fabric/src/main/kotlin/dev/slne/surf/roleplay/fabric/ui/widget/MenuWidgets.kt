package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeTokens
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * The metrics shared by menu entries.
 */
object MenuStyle {
    /**
     * The height of an item.
     */
    const val ITEM_HEIGHT: Int = 16

    /**
     * The space left and right inside an item.
     */
    const val PADDING_X: Int = 4

    /**
     * The indent of the text of inset items and of items with an icon or indicator.
     */
    const val INSET: Int = 16

    /**
     * The size of item icons and indicators.
     */
    const val ICON: Int = 8

    /**
     * The smallest space between an item's text and its shortcut or arrow.
     */
    const val SHORTCUT_GAP: Int = 16

    /**
     * The opacity of the highlight of destructive items in dark themes.
     */
    private const val DESTRUCTIVE_DARK_ALPHA: Float = 0.2f

    /**
     * The opacity of the highlight of destructive items in light themes.
     */
    private const val DESTRUCTIVE_LIGHT_ALPHA: Float = 0.1f

    /**
     * Returns the highlight colour of an item.
     *
     * @param tokens the theme tokens
     * @param destructive whether the item is destructive
     * @return the colour
     */
    fun highlight(tokens: ThemeTokens, destructive: Boolean): Int = if (destructive) {
        ThemeColors.withAlpha(tokens.destructive, if (ThemeColors.isDark(tokens.background)) DESTRUCTIVE_DARK_ALPHA else DESTRUCTIVE_LIGHT_ALPHA)
    } else {
        tokens.accent
    }
}

/**
 * An entry of a menu that can be highlighted and chosen: highlighted while the mouse is on it or
 * it has the focus, and chosen by a click, Enter or Space.
 *
 * @param id the id of the widget
 */
abstract class MenuEntryWidget(id: String) : Widget(id) {

    /**
     * Whether the entry can take the focus: while it is enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * Entries draw their highlight instead of a focus ring.
     */
    override val drawsOwnFocus: Boolean get() = true

    /**
     * Whether the entry is drawn in the destructive colour.
     */
    open val destructive: Boolean get() = false

    /**
     * Chooses the entry.
     *
     * @param context the screen showing the entry
     */
    abstract fun choose(context: UiContext)

    /**
     * Checks whether the entry is highlighted.
     *
     * @param context the screen showing the entry
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @return whether the mouse is on the entry or it has the focus
     */
    open fun highlighted(context: UiContext, mouseX: Int, mouseY: Int): Boolean = enabled && (isOver(mouseX, mouseY) || context.focusedWidget === this)

    /**
     * Draws the highlight of the entry if it is highlighted.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the entry
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @return whether the entry is highlighted
     */
    protected fun drawHighlight(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int): Boolean {
        val lit = highlighted(context, mouseX, mouseY)
        if (lit) ui.fillRounded(bounds, MenuStyle.highlight(ui.tokens, destructive), 2)
        return lit
    }

    /**
     * Returns the colour of the entry's text.
     *
     * @param ui the graphics to draw with
     * @param lit whether the entry is highlighted
     * @return the colour, dimmed while disabled
     */
    protected fun textColor(ui: UiGraphics, lit: Boolean): Int {
        val tokens = ui.tokens
        val color = when {
            destructive -> tokens.destructive
            lit -> tokens.accentForeground
            else -> tokens.popoverForeground
        }
        return if (enabled) color else ui.disabled(color)
    }

    /**
     * Chooses the entry on a left click.
     *
     * @param context the screen showing the entry
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on the entry
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            choose(context)
        }
        return true
    }

    /**
     * Chooses the entry on Enter or Space.
     *
     * @param context the screen showing the entry
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled || !isActivation(event)) return false
        choose(context)
        return true
    }

    /**
     * Draws the text of a one-line entry after its indent, and an optional text at its end.
     *
     * @param ui the graphics to draw with
     * @param text the text as component JSON
     * @param indent whether the text is indented
     * @param end the text at the end as component JSON, or `null` for none
     * @param color the colour of the text
     */
    protected fun drawLine(ui: UiGraphics, text: String, indent: Boolean, end: String?, color: Int) {
        val y = bounds.y + (bounds.height - ui.lineHeight + 1) / 2
        ui.text(text, bounds.x + MenuStyle.PADDING_X + if (indent) MenuStyle.INSET - MenuStyle.PADDING_X else 0, y, color)
        end?.let { ui.text(it, bounds.right - MenuStyle.PADDING_X - ui.width(it), y, ui.tokens.mutedForeground) }
    }

    /**
     * Returns the size of a one-line entry.
     *
     * @param measurer the text measurer
     * @param text the text as component JSON
     * @param indent whether the text is indented
     * @param endWidth the width of the text or arrow at the end, or `0` for none
     * @return the size
     */
    protected fun lineSize(measurer: TextMeasurer, text: String, indent: Boolean, endWidth: Int): Size {
        val start = if (indent) MenuStyle.INSET else MenuStyle.PADDING_X
        val end = if (endWidth > 0) MenuStyle.SHORTCUT_GAP + endWidth else 0
        return Size(start + measurer.width(text) + end + MenuStyle.PADDING_X, MenuStyle.ITEM_HEIGHT)
    }
}

/**
 * An item of a menu that fires a widget action when chosen.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property shortcut a shortcut shown at the end as component JSON, or `null` for none
 * @property destructive whether the item is drawn in the destructive colour
 * @property inset whether the text is indented as if it had an icon
 */
class MenuItemWidget(
    id: String,
    var text: String,
    val icon: String?,
    val shortcut: String?,
    override val destructive: Boolean,
    val inset: Boolean,
) : MenuEntryWidget(id) {

    /**
     * Returns the size of the icon, text and shortcut on one line.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = lineSize(measurer, text, inset || icon != null, shortcut?.let { measurer.width(it) } ?: 0)

    /**
     * Draws the highlight, the icon, the text and the shortcut.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val lit = drawHighlight(ui, context, mouseX, mouseY)
        icon?.let {
            val color = if (destructive) ui.tokens.destructive else ui.tokens.mutedForeground
            ui.icon(it, Rect(bounds.x + MenuStyle.PADDING_X, bounds.y + (bounds.height - MenuStyle.ICON) / 2, MenuStyle.ICON, MenuStyle.ICON), if (enabled) color else ui.disabled(color))
        }
        drawLine(ui, text, inset || icon != null, shortcut, textColor(ui, lit))
    }

    /**
     * Fires the item's action.
     *
     * @param context the screen showing the item
     */
    override fun choose(context: UiContext) = context.actionTriggered(this, submitsInput = false)

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
 * An item of a menu with a check mark. Choosing it flips the mark and fires a widget action; its
 * input value is `true` or `false`.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property checked whether the item is checked
 */
class MenuCheckboxItemWidget(id: String, var text: String, var checked: Boolean) : MenuEntryWidget(id) {

    /**
     * The check state as `true` or `false`.
     */
    override val inputValue: String get() = checked.toString()

    /**
     * Sets the check state from `true` or `false`.
     *
     * @param value the value
     */
    override fun applyValue(value: String) {
        checked = value == "true"
    }

    /**
     * Returns the size of the indicator and the text on one line.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = lineSize(measurer, text, true, 0)

    /**
     * Draws the highlight, the check mark while checked, and the text.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val lit = drawHighlight(ui, context, mouseX, mouseY)
        val color = textColor(ui, lit)
        if (checked) ui.icon("check", Rect(bounds.x + MenuStyle.PADDING_X, bounds.y + (bounds.height - MenuStyle.ICON) / 2, MenuStyle.ICON, MenuStyle.ICON), color)
        drawLine(ui, text, true, null, color)
    }

    /**
     * Flips the mark and fires the item's action.
     *
     * @param context the screen showing the item
     */
    override fun choose(context: UiContext) {
        checked = !checked
        touched = true
        context.actionTriggered(this, submitsInput = false)
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
 * A group of radio items of which one is chosen. Its input value is the chosen item's value, and
 * choosing an item fires the group's widget action.
 *
 * @param id the id of the widget
 * @property value the value of the chosen item, or empty for none
 */
class MenuRadioGroupWidget(id: String, var value: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }

    /**
     * The chosen value.
     */
    override val inputValue: String get() = value

    /**
     * Chooses a value set by the server.
     *
     * @param value the value
     */
    override fun applyValue(value: String) {
        this.value = value
    }

    /**
     * Links the radio items to this group, then creates the layout box of the group.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        link()
        return super.createLayout(measurer)
    }

    /**
     * Links the radio items to this group.
     */
    fun link() = childList.filterIsInstance<MenuRadioItemWidget>().forEach { it.group = this }

    /**
     * Chooses a value and fires the group's action.
     *
     * @param context the screen showing the group
     * @param chosen the value
     */
    fun select(context: UiContext, chosen: String) {
        value = chosen
        touched = true
        context.actionTriggered(this, submitsInput = false)
    }
}

/**
 * An item of a radio group, marked with a dot while its value is chosen.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property value the value the item stands for
 */
class MenuRadioItemWidget(id: String, var text: String, val value: String) : MenuEntryWidget(id) {

    /**
     * The group the item belongs to, linked by the group before each layout.
     */
    var group: MenuRadioGroupWidget? = null

    /**
     * Returns the size of the indicator and the text on one line.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = lineSize(measurer, text, true, 0)

    /**
     * Draws the highlight, the dot while chosen, and the text.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val lit = drawHighlight(ui, context, mouseX, mouseY)
        val color = textColor(ui, lit)
        if (group?.value == value) {
            val dot = MenuStyle.ICON / 2
            ui.fillRounded(Rect(bounds.x + MenuStyle.PADDING_X + dot / 2, bounds.y + (bounds.height - dot) / 2, dot, dot), color, dot / 2)
        }
        drawLine(ui, text, true, null, color)
    }

    /**
     * Chooses this item's value in its group.
     *
     * @param context the screen showing the item
     */
    override fun choose(context: UiContext) {
        group?.select(context, value)
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
 * A heading inside a menu.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property inset whether the text is indented as if it had an icon
 */
class MenuLabelWidget(id: String, var text: String, val inset: Boolean) : Widget(id) {

    /**
     * Returns the size of the bold text on one line.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        Size((if (inset) MenuStyle.INSET else MenuStyle.PADDING_X) + measurer.width(TextStyle.styled(text, bold = true, italic = false)) + MenuStyle.PADDING_X, MenuStyle.ITEM_HEIGHT)

    /**
     * Draws the text.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val x = bounds.x + if (inset) MenuStyle.INSET else MenuStyle.PADDING_X
        ui.text(TextStyle.styled(text, bold = true, italic = false), x, bounds.y + (bounds.height - ui.lineHeight + 1) / 2, ui.tokens.popoverForeground)
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
 * A line between the parts of a menu, across its full width.
 *
 * @param id the id of the widget
 */
class MenuSeparatorWidget(id: String) : Widget(id) {

    /**
     * Returns the height of the line with its space.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(0, HEIGHT)

    /**
     * Draws the line through the middle of the widget, reaching into the menu's padding.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.fill(Rect(bounds.x - MenuContentWidget.PADDING, bounds.y + bounds.height / 2, bounds.width + 2 * MenuContentWidget.PADDING, 1), ui.tokens.border)
    }

    /**
     * Holds the separator height.
     */
    private companion object {
        /**
         * The height of the separator with the space around the line.
         */
        const val HEIGHT: Int = 5
    }
}

/**
 * A group of related menu entries.
 *
 * @param id the id of the widget
 */
class MenuGroupWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }
}

/**
 * The content of a menu: a stack of entries on a bordered surface.
 *
 * @param id the id of the widget
 */
class MenuContentWidget(id: String) : OverlayContentWidget(id) {
    init {
        padding = Insets(PADDING, PADDING, PADDING, PADDING)
    }

    /**
     * Draws the surface, then the entries.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        OverlaySurface.draw(ui, bounds)
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Holds the menu padding.
     */
    companion object {
        /**
         * The space inside the surface.
         */
        const val PADDING: Int = 2
    }
}

/**
 * The item of a menu that opens a sub-menu, marked with an arrow and highlighted while its
 * sub-menu is open.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property inset whether the text is indented as if it had an icon
 */
class MenuSubTriggerWidget(id: String, var text: String, val icon: String?, val inset: Boolean) : MenuEntryWidget(id) {

    /**
     * The sub-menu this trigger opens, linked by the sub-menu before each layout.
     */
    var sub: MenuSubWidget? = null

    /**
     * Checks whether the trigger is highlighted: also while its sub-menu is open.
     *
     * @param context the screen showing the entry
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @return whether the trigger is highlighted
     */
    override fun highlighted(context: UiContext, mouseX: Int, mouseY: Int): Boolean = super.highlighted(context, mouseX, mouseY) || sub?.open == true

    /**
     * Returns the size of the icon, text and arrow on one line.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = lineSize(measurer, text, inset || icon != null, MenuStyle.ICON)

    /**
     * Draws the highlight, the icon, the text and the arrow.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val lit = drawHighlight(ui, context, mouseX, mouseY)
        val iconY = bounds.y + (bounds.height - MenuStyle.ICON) / 2
        icon?.let { ui.icon(it, Rect(bounds.x + MenuStyle.PADDING_X, iconY, MenuStyle.ICON, MenuStyle.ICON), ui.tokens.mutedForeground) }
        val color = textColor(ui, lit)
        drawLine(ui, text, inset || icon != null, null, color)
        ui.icon("chevron-right", Rect(bounds.right - MenuStyle.PADDING_X - MenuStyle.ICON, iconY, MenuStyle.ICON, MenuStyle.ICON), color)
    }

    /**
     * Asks the screen to toggle the sub-menu.
     *
     * @param context the screen showing the trigger
     */
    override fun choose(context: UiContext) = context.actionTriggered(this, submitsInput = false)

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
 * A menu overlay: the content of a dropdown menu, context menu, menubar menu or sub-menu. Up and
 * Down move the highlight through its entries, Right opens a sub-menu from its trigger, Left
 * closes a sub-menu, and in a menubar Left and Right switch between its menus. Pointing at an
 * entry highlights it, opens its sub-menu and closes other sub-menus. Choosing an entry closes
 * every menu.
 *
 * @param owner the host that opened the menu
 * @param side the side of the anchor the menu opens on
 * @param align how the menu is aligned along that side
 * @param offset the space between the anchor and the menu
 */
open class MenuPopover(owner: OverlayHostWidget, side: OverlaySide, align: Align, offset: Int = 4) : WidgetPopover(owner, side, align, offset) {

    /**
     * The mouse position of the last frame, so that only movement moves the highlight.
     */
    private var lastMouse: Pair<Int, Int>? = null

    /**
     * Returns the entries of the menu that can take the focus, in order, leaving out the entries
     * of closed sub-menus.
     *
     * @return the entries
     */
    fun entries(): List<MenuEntryWidget> {
        val found = mutableListOf<MenuEntryWidget>()
        fun collect(widget: Widget) {
            if (widget is MenuEntryWidget && widget.focusable) found += widget
            widget.focusChildren.forEach(::collect)
        }
        content?.let(::collect)
        return found
    }

    /**
     * Handles the menu keys.
     *
     * @param context the screen showing the menu
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        val entries = entries()
        val index = entries.indexOf(context.focusedWidget)
        when (event.key()) {
            GLFW.GLFW_KEY_DOWN -> context.focus(if (index < 0) entries.firstOrNull() else entries[(index + 1).coerceAtMost(entries.lastIndex)])
            GLFW.GLFW_KEY_UP -> context.focus(if (index < 0) entries.lastOrNull() else entries[(index - 1).coerceAtLeast(0)])
            GLFW.GLFW_KEY_HOME -> context.focus(entries.firstOrNull())
            GLFW.GLFW_KEY_END -> context.focus(entries.lastOrNull())
            GLFW.GLFW_KEY_RIGHT -> {
                val trigger = entries.getOrNull(index) as? MenuSubTriggerWidget
                val sub = trigger?.sub
                when {
                    sub != null -> openSub(context, sub)
                    owner is MenubarMenuWidget -> (owner as MenubarMenuWidget).switchTo(context, 1)
                    else -> return false
                }
            }
            GLFW.GLFW_KEY_LEFT -> when (val host = owner) {
                is MenuSubWidget -> {
                    host.close(context)
                    context.focus(host.triggers.firstOrNull())
                }
                is MenubarMenuWidget -> host.switchTo(context, -1)
                else -> return false
            }
            else -> return false
        }
        return true
    }

    /**
     * Opens a sub-menu and highlights its first entry.
     *
     * @param context the screen showing the menu
     * @param sub the sub-menu
     */
    private fun openSub(context: UiContext, sub: MenuSubWidget) {
        sub.show(context, report = true)
        val opened = context.popovers.lastOrNull { it.owner === sub } as? MenuPopover
        context.focus(opened?.entries()?.firstOrNull())
    }

    /**
     * Reacts to the mouse resting on the menu: highlights the entry under it, opens that entry's
     * sub-menu, and closes sub-menus of other entries.
     *
     * @param context the screen showing the menu
     * @param x the mouse x position
     * @param y the mouse y position
     */
    fun pointerMoved(context: UiContext, x: Int, y: Int) {
        val hit = entries().firstOrNull { it.isOver(x, y) } ?: return
        val above = context.popovers.dropWhile { it !== this }.drop(1).firstOrNull()
        val sub = (hit as? MenuSubTriggerWidget)?.sub
        if (above != null && above.owner !== sub) context.closePopover(above)
        if (context.focusedWidget !== hit) context.focus(hit)
        if (sub != null && !sub.open) sub.show(context, report = true)
    }

    /**
     * Draws the menu and follows the mouse while it moves on the menu.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the menu
     * @param area the menu's area
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, area: Rect, mouseX: Int, mouseY: Int) {
        super.render(ui, context, area, mouseX, mouseY)
        val mouse = mouseX to mouseY
        if (mouse != lastMouse && area.contains(mouseX.toDouble(), mouseY.toDouble())) pointerMoved(context, mouseX, mouseY)
        lastMouse = mouse
    }

    /**
     * Closes every menu after an entry fired its action.
     *
     * @param context the screen showing the menu
     * @param widget the entry
     */
    override fun afterAction(context: UiContext, widget: Widget) {
        context.popovers.firstOrNull { it is MenuPopover }?.let { context.closePopover(it) }
    }
}

/**
 * A dropdown menu: its triggers open a menu next to them.
 *
 * @param id the id of the widget
 * @property side the side of the triggers the menu opens on
 * @property align how the menu is aligned along that side
 */
class DropdownMenuWidget(id: String, val side: OverlaySide, val align: Align) : OverlayHostWidget(id) {

    /**
     * Creates the menu overlay.
     *
     * @return the overlay
     */
    override fun createPopover(): Popover = MenuPopover(this, side, align)
}

/**
 * A sub-menu: its trigger opens a menu beside it.
 *
 * @param id the id of the widget
 */
class MenuSubWidget(id: String) : OverlayHostWidget(id, Axis.VERTICAL) {

    /**
     * Links the trigger to this sub-menu, then creates the layout box of the trigger.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        link()
        return super.createLayout(measurer)
    }

    /**
     * Links the trigger to this sub-menu.
     */
    fun link() = triggers.filterIsInstance<MenuSubTriggerWidget>().forEach { it.sub = this }

    /**
     * Creates the menu overlay beside the trigger.
     *
     * @return the overlay
     */
    override fun createPopover(): Popover = MenuPopover(this, OverlaySide.RIGHT, Align.START, SUB_OFFSET)

    /**
     * Holds the sub-menu offset.
     */
    private companion object {
        /**
         * The space between the trigger and the sub-menu.
         */
        const val SUB_OFFSET: Int = 2
    }
}

/**
 * A context menu: a right click in its area opens a menu at the mouse.
 *
 * @param id the id of the widget
 */
class ContextMenuWidget(id: String) : OverlayHostWidget(id, Axis.VERTICAL) {

    /**
     * Actions inside the area stay actions; the overlay opens on a right click.
     */
    override val togglesOnTriggerAction: Boolean get() = false

    /**
     * Where the last right click was.
     */
    private var point: Rect = Rect.EMPTY

    /**
     * The menu opens at the last right click.
     *
     * @return the point of the click as an empty area
     */
    override fun anchor(): Rect = point

    /**
     * Opens the menu at the mouse on a right click in the area, and passes other clicks to the
     * area.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was handled
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && isOver(x, y) && enabled) {
            point = Rect(x.toInt(), y.toInt(), 0, 0)
            if (open) close(context)
            show(context, report = true)
            return true
        }
        return super.mouseClicked(context, x, y, button)
    }

    /**
     * Creates the menu overlay at the click.
     *
     * @return the overlay
     */
    override fun createPopover(): Popover = MenuPopover(this, OverlaySide.BOTTOM, Align.START, 0)
}

/**
 * A horizontal bar of menus on a bordered surface.
 *
 * @param id the id of the widget
 */
class MenubarWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = 2
        padding = Insets(2, 2, 2, 2)
        crossAlign = Align.CENTER
    }

    /**
     * Links the menus to this bar, then creates the layout box of the bar.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        childList.filterIsInstance<MenubarMenuWidget>().forEach { it.bar = this }
        return super.createLayout(measurer)
    }

    /**
     * Draws the surface, then the menus.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.fillRounded(bounds, ui.tokens.background)
        ui.borderRounded(bounds, ui.tokens.border)
        super.render(ui, context, mouseX, mouseY)
    }
}

/**
 * A menu of a menubar: its trigger opens a menu below it, and pointing at it while another menu
 * of the bar is open switches to it.
 *
 * @param id the id of the widget
 */
class MenubarMenuWidget(id: String) : OverlayHostWidget(id) {

    /**
     * The bar the menu belongs to, linked by the bar before each layout.
     */
    var bar: MenubarWidget? = null

    /**
     * Creates the menu overlay below the trigger.
     *
     * @return the overlay
     */
    override fun createPopover(): Popover = MenuPopover(this, OverlaySide.BOTTOM, Align.START)

    /**
     * Switches to this menu if the mouse is on it while another menu of the same bar is open.
     *
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    fun hoverSwitch(context: UiContext, mouseX: Int, mouseY: Int) {
        if (open || !isOver(mouseX, mouseY)) return
        val other = context.popovers.map { it.owner }.filterIsInstance<MenubarMenuWidget>().firstOrNull { it !== this && it.bar === bar } ?: return
        other.close(context)
        show(context, report = true)
    }

    /**
     * Closes this menu and opens a neighbouring menu of the bar.
     *
     * @param context the screen showing the widget
     * @param step `1` for the next menu, `-1` for the previous one, wrapping at the ends
     */
    fun switchTo(context: UiContext, step: Int) {
        val menus = bar?.childList?.filterIsInstance<MenubarMenuWidget>() ?: return
        val index = menus.indexOf(this)
        if (index < 0 || menus.size < 2) return
        val next = menus[(index + step + menus.size) % menus.size]
        close(context)
        next.show(context, report = true)
        val opened = context.popovers.lastOrNull { it.owner === next } as? MenuPopover
        context.focus(opened?.entries()?.firstOrNull())
    }

    /**
     * Draws the trigger and switches menus on hover.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        super.render(ui, context, mouseX, mouseY)
        hoverSwitch(context, mouseX, mouseY)
    }
}

/**
 * The trigger of a menubar menu: a text highlighted while the mouse is on it or its menu is open.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 */
class MenubarTriggerWidget(id: String, var text: String) : Widget(id) {

    /**
     * Whether the trigger can take the focus: while it is enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * Returns the size of the bold text with padding.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(measurer.width(TextStyle.styled(text, bold = true, italic = false)) + 2 * PADDING_X, HEIGHT)

    /**
     * Draws the highlight while hovered or while the menu is open, and the text.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val open = context.popovers.any { (it.owner as? OverlayHostWidget)?.triggers?.contains(this) == true }
        val lit = open || isOver(mouseX, mouseY)
        if (lit) ui.fillRounded(bounds, ui.tokens.accent, 2)
        ui.centeredText(TextStyle.styled(text, bold = true, italic = false), bounds, if (lit) ui.tokens.accentForeground else ui.tokens.foreground)
    }

    /**
     * Asks the screen to toggle the menu on a left click.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on the trigger
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            context.actionTriggered(this, submitsInput = false)
        }
        return true
    }

    /**
     * Asks the screen to toggle the menu on Enter, Space or Down.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled || !(isActivation(event) || event.key() == GLFW.GLFW_KEY_DOWN)) return false
        context.actionTriggered(this, submitsInput = false)
        return true
    }

    /**
     * Replaces the text.
     *
     * @param json the new text as component JSON
     */
    override fun applyText(json: String) {
        text = json
    }

    /**
     * Holds the trigger metrics.
     */
    private companion object {
        /**
         * The space left and right of the text.
         */
        const val PADDING_X: Int = 4

        /**
         * The height of the trigger.
         */
        const val HEIGHT: Int = 14
    }
}

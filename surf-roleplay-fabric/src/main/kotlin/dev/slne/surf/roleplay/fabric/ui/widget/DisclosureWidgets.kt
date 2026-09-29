package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.protocol.screen.AccordionType
import dev.slne.surf.roleplay.protocol.screen.Align
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * A collapsible: its triggers, and contents that are shown while it is open. An action inside a
 * trigger toggles it. Its open state is its input value, `true` or `false`.
 *
 * @param id the id of the widget
 */
class CollapsibleWidget(id: String) : ContainerWidget(id, Axis.VERTICAL), ActionInterceptor {
    init {
        crossAlign = Align.STRETCH
    }

    /**
     * Whether the contents are shown.
     */
    var open: Boolean = false
        private set

    /**
     * The open state as `true` or `false`.
     */
    override val inputValue: String get() = open.toString()

    /**
     * Opens or closes the collapsible as the server asks, without reporting the change.
     *
     * @param value `true` to open it, anything else to close it
     */
    override fun applyValue(value: String) = setOpen(value == "true")

    /**
     * Shows or hides the contents.
     *
     * @param open whether the contents are shown
     */
    fun setOpen(open: Boolean) {
        this.open = open
        childList.filterIsInstance<CollapsibleContentWidget>().forEach { it.hidden = !open }
    }

    /**
     * Toggles the collapsible when an action fires inside one of its triggers, and reports the
     * change.
     *
     * @param context the screen showing the widget
     * @param widget the widget whose action fired
     * @param via the child holding the widget
     * @return whether the action came from a trigger
     */
    override fun interceptAction(context: UiContext, widget: Widget, via: Widget): Boolean {
        if (via !is CollapsibleTriggerWidget) return false
        if (enabled) {
            setOpen(!open)
            markChanged(context, immediate = true)
            context.requestLayout()
        }
        return true
    }
}

/**
 * The trigger part of a collapsible: the actions of the widgets inside it toggle the
 * collapsible.
 *
 * @param id the id of the widget
 */
class CollapsibleTriggerWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        crossAlign = Align.CENTER
    }
}

/**
 * The content of a collapsible, hidden while the collapsible is closed.
 *
 * @param id the id of the widget
 */
class CollapsibleContentWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
        hidden = true
    }
}

/**
 * An accordion: stacked items, each shown or hidden by its trigger. Its input value is the
 * values of the open items in the order of the items, comma separated.
 *
 * @param id the id of the widget
 * @property type how many items can be open at once
 * @property collapsible whether the open item of a single accordion can be closed
 */
class AccordionWidget(id: String, val type: AccordionType, val collapsible: Boolean) : ContainerWidget(id, Axis.VERTICAL), ActionInterceptor, KeyInterceptor {
    init {
        crossAlign = Align.STRETCH
    }

    /**
     * The items, in order.
     */
    val items: List<AccordionItemWidget> get() = childList.filterIsInstance<AccordionItemWidget>()

    /**
     * The values of the open items, comma separated.
     */
    override val inputValue: String get() = items.filter { it.open }.joinToString(",") { it.value }

    /**
     * Opens the items the server names and closes the others, without reporting the change.
     *
     * @param value the values of the items to open, comma separated
     */
    override fun applyValue(value: String) = open(value.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet())

    /**
     * Opens the items with the given values and closes the others. A single accordion opens only
     * the first of them.
     *
     * @param values the values of the items to open
     */
    fun open(values: Set<String>) {
        var left = if (type == AccordionType.SINGLE) 1 else Int.MAX_VALUE
        items.forEach { item ->
            val opens = item.value in values && left > 0
            if (opens) left--
            item.setOpen(opens)
        }
    }

    /**
     * Toggles an item when its trigger fires: in a single accordion opening an item closes the
     * open one, and the open item stays open unless the accordion is collapsible.
     *
     * @param context the screen showing the widget
     * @param widget the widget whose action fired
     * @param via the child holding the widget
     * @return whether the action came from an item trigger
     */
    override fun interceptAction(context: UiContext, widget: Widget, via: Widget): Boolean {
        if (widget !is AccordionTriggerWidget || via !is AccordionItemWidget) return false
        if (!via.enabled || !enabled) return true
        val before = inputValue
        when {
            !via.open && type == AccordionType.SINGLE -> items.forEach { it.setOpen(it === via) }
            !via.open -> via.setOpen(true)
            type == AccordionType.MULTIPLE || collapsible -> via.setOpen(false)
        }
        if (inputValue != before) {
            markChanged(context, immediate = true)
            context.requestLayout()
        }
        return true
    }

    /**
     * Moves the focus between the triggers of the enabled items on Up, Down, Home and End,
     * wrapping around, while one of them has the focus.
     *
     * @param context the screen showing the widget
     * @param focused the focused widget
     * @param event the key event
     * @return whether the focus moved
     */
    override fun descendantKeyPressed(context: UiContext, focused: Widget, event: KeyEvent): Boolean {
        val triggers = items.filter { it.enabled }.mapNotNull { it.trigger }
        val current = triggers.indexOf(focused)
        if (current < 0) return false
        val next = when (event.key()) {
            GLFW.GLFW_KEY_DOWN -> (current + 1) % triggers.size
            GLFW.GLFW_KEY_UP -> (current - 1 + triggers.size) % triggers.size
            GLFW.GLFW_KEY_HOME -> 0
            GLFW.GLFW_KEY_END -> triggers.lastIndex
            else -> return false
        }
        context.focus(triggers[next])
        return true
    }

    /**
     * Draws the items, with a border below every item but the last.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        super.render(ui, context, mouseX, mouseY)
        val shown = items.filter { !it.hidden }
        shown.dropLast(1).forEach { item -> ui.fill(Rect(item.bounds.x, item.bounds.bottom - 1, item.bounds.width, 1), ui.tokens.border) }
    }
}

/**
 * An item of an accordion: its trigger and its content, which is hidden while the item is
 * closed.
 *
 * @param id the id of the widget
 * @property value the value that identifies the item in its accordion
 */
class AccordionItemWidget(id: String, val value: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }

    /**
     * Whether the content is shown.
     */
    var open: Boolean = false
        private set

    /**
     * The trigger of the item, or `null` if it has none.
     */
    val trigger: AccordionTriggerWidget? get() = childList.firstOrNull { it is AccordionTriggerWidget } as AccordionTriggerWidget?

    /**
     * Shows or hides the content.
     *
     * @param open whether the content is shown
     */
    fun setOpen(open: Boolean) {
        this.open = open
        childList.filterIsInstance<AccordionContentWidget>().forEach { it.hidden = !open }
    }
}

/**
 * The trigger of an accordion item: its text, underlined while hovered, and a chevron at the end
 * that points up while the item is open.
 *
 * @param id the id of the widget
 * @param text the text as component JSON
 */
class AccordionTriggerWidget(id: String, var text: String) : Widget(id) {

    /**
     * The item this trigger opens, set by the factory.
     */
    var item: AccordionItemWidget? = null

    /**
     * Whether the trigger can take the focus: while it and its item are enabled.
     */
    override val focusable: Boolean get() = enabled && item?.enabled != false

    /**
     * Returns the size of the text and the chevron with the vertical padding.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        Size(measurer.width(text) + GAP + CHEVRON, measurer.lineHeight + 2 * PADDING_Y)

    /**
     * Draws the text, underlined while hovered, and the chevron.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val usable = focusable
        val color = if (usable) ui.tokens.foreground else ThemeColors.withAlpha(ui.tokens.foreground, DISABLED_ALPHA)
        val textY = bounds.y + PADDING_Y
        ui.text(text, mirroredX(bounds.x, ui.width(text)), textY, color)
        if (usable && isOver(mouseX, mouseY)) {
            val width = ui.width(text).coerceAtMost(bounds.width - GAP - CHEVRON)
            ui.fill(mirrored(Rect(bounds.x, textY + ui.lineHeight, width, 1)), color)
        }
        val chevron = mirrored(Rect(bounds.right - CHEVRON, textY, CHEVRON, CHEVRON))
        ui.rotatedIcon("chevron-down", chevron, ui.tokens.mutedForeground, if (item?.open == true) 180f else 0f)
    }

    /**
     * Focuses the trigger and asks the screen to toggle its item on a left click.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on the trigger
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (focusable && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            context.actionTriggered(this, submitsInput = false)
        }
        return true
    }

    /**
     * Asks the screen to toggle the item on Enter or Space.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!focusable || !isActivation(event)) return false
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
    companion object {
        /**
         * The space above and below the text.
         */
        const val PADDING_Y: Int = 8

        /**
         * The space between the text and the chevron.
         */
        const val GAP: Int = 8

        /**
         * The size of the chevron.
         */
        const val CHEVRON: Int = 8

        /**
         * The opacity of the text of a disabled trigger.
         */
        private const val DISABLED_ALPHA: Float = 0.5f
    }
}

/**
 * The content of an accordion item, with space below it, hidden while the item is closed.
 *
 * @param id the id of the widget
 */
class AccordionContentWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
        padding = Insets(bottom = PADDING_BOTTOM)
        hidden = true
    }

    /**
     * Holds the content metrics.
     */
    companion object {
        /**
         * The space below the content.
         */
        const val PADDING_BOTTOM: Int = 8
    }
}

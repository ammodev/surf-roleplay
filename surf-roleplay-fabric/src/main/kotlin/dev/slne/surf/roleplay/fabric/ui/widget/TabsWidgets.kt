package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.TabsVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * Tabs: a tab list and contents, of which only the content of the selected tab is shown. The
 * value of the selected tab is its input value.
 *
 * Horizontal tabs place the list above the contents; vertical tabs place it beside them.
 *
 * @param id the id of the widget
 * @property orientation where the triggers are
 */
class TabsWidget(id: String, val orientation: Orientation) :
    ContainerWidget(id, if (orientation == Orientation.HORIZONTAL) Axis.VERTICAL else Axis.HORIZONTAL),
    ActionInterceptor,
    KeyInterceptor {

    init {
        gap = GAP
        crossAlign = if (orientation == Orientation.HORIZONTAL) Align.STRETCH else Align.START
    }

    /**
     * The value of the selected tab.
     */
    var selected: String = ""
        private set

    /**
     * The triggers of every tab list of the tabs, in order.
     */
    val triggers: List<TabsTriggerWidget>
        get() = childList.filterIsInstance<TabsListWidget>().flatMap { list -> list.childList.filterIsInstance<TabsTriggerWidget>() }

    /**
     * The value of the selected tab.
     */
    override val inputValue: String get() = selected

    /**
     * Selects the tab the server names, without reporting the change.
     *
     * @param value the value of the tab
     */
    override fun applyValue(value: String) = select(value)

    /**
     * Selects a tab: shows the contents with its value and hides the others. An empty or unknown
     * value selects the first enabled tab.
     *
     * @param value the value of the tab
     */
    fun select(value: String) {
        val known = triggers.map { it.value }
        selected = if (value in known) value else triggers.firstOrNull { it.enabled }?.value ?: value
        childList.filterIsInstance<TabsContentWidget>().forEach { it.hidden = it.value != selected }
    }

    /**
     * Selects a tab when its trigger fires and reports the change.
     *
     * @param context the screen showing the widget
     * @param widget the widget whose action fired
     * @param via the child holding the widget
     * @return whether the action came from a trigger
     */
    override fun interceptAction(context: UiContext, widget: Widget, via: Widget): Boolean {
        if (widget !is TabsTriggerWidget || via !is TabsListWidget) return false
        choose(context, widget)
        return true
    }

    /**
     * Selects the tab of a trigger if it is enabled and not selected yet, and reports the change.
     *
     * @param context the screen showing the widget
     * @param trigger the trigger
     */
    private fun choose(context: UiContext, trigger: TabsTriggerWidget) {
        if (!enabled || !trigger.enabled || trigger.value == selected) return
        select(trigger.value)
        markChanged(context, immediate = true)
        context.requestLayout()
    }

    /**
     * Moves the focus between the enabled triggers and selects the focused tab: Right and Left for
     * horizontal tabs, Down and Up for vertical ones, wrapping around, and Home and End.
     *
     * @param context the screen showing the widget
     * @param focused the focused widget
     * @param event the key event
     * @return whether the focus moved
     */
    override fun descendantKeyPressed(context: UiContext, focused: Widget, event: KeyEvent): Boolean {
        val usable = triggers.filter { it.enabled }
        val current = usable.indexOf(focused)
        if (current < 0) return false
        val forward = if (orientation == Orientation.HORIZONTAL) GLFW.GLFW_KEY_RIGHT else GLFW.GLFW_KEY_DOWN
        val backward = if (orientation == Orientation.HORIZONTAL) GLFW.GLFW_KEY_LEFT else GLFW.GLFW_KEY_UP
        val next = when (event.key()) {
            forward -> (current + 1) % usable.size
            backward -> (current - 1 + usable.size) % usable.size
            GLFW.GLFW_KEY_HOME -> 0
            GLFW.GLFW_KEY_END -> usable.lastIndex
            else -> return false
        }
        context.focus(usable[next])
        choose(context, usable[next])
        return true
    }

    /**
     * Holds the tabs metrics.
     */
    companion object {
        /**
         * The space between the list and the contents.
         */
        const val GAP: Int = 4
    }
}

/**
 * The list of the triggers of tabs. The default variant draws a muted pill around the triggers;
 * the line variant draws nothing of its own.
 *
 * @param id the id of the widget
 * @property variant how the list is drawn
 * @param axis the axis the triggers are laid out along
 */
class TabsListWidget(id: String, val variant: TabsVariant, axis: Axis) : ContainerWidget(id, axis) {
    init {
        padding = if (variant == TabsVariant.DEFAULT) Insets(PADDING, PADDING, PADDING, PADDING) else Insets.NONE
        gap = if (variant == TabsVariant.LINE) LINE_GAP else 0
        crossAlign = if (axis == Axis.VERTICAL) Align.STRETCH else Align.CENTER
    }

    /**
     * Draws the pill of the default variant around the triggers, and the triggers.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val shown = shownChildren
        if (variant == TabsVariant.DEFAULT && shown.isNotEmpty()) {
            val right = shown.maxOf { it.bounds.right } + PADDING
            val bottom = shown.maxOf { it.bounds.bottom } + PADDING
            ui.fillRounded(Rect(bounds.x, bounds.y, right - bounds.x, bottom - bounds.y), ui.tokens.muted, ui.tokens.radius + 1)
        }
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Holds the list metrics.
     */
    companion object {
        /**
         * The space between the pill of the default variant and its triggers.
         */
        const val PADDING: Int = 2

        /**
         * The space between the triggers of the line variant.
         */
        const val LINE_GAP: Int = 2
    }
}

/**
 * A trigger of tabs: its icon and text. Selecting it shows the content with the same value. The
 * selected trigger is raised in the default variant and underlined in the line variant.
 *
 * @param id the id of the widget
 * @property value the value of the tab
 * @param text the text as component JSON
 * @property icon the Lucide name of an icon before the text, or `null` for none
 */
class TabsTriggerWidget(id: String, val value: String, var text: String, val icon: String?) : Widget(id) {

    /**
     * The tabs this trigger belongs to, set by the factory.
     */
    var tabs: TabsWidget? = null

    /**
     * The list this trigger is in, set by the factory.
     */
    var list: TabsListWidget? = null

    /**
     * Whether the trigger can take the focus: while it is enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * Whether this trigger's tab is selected.
     */
    val active: Boolean get() = tabs?.selected == value

    /**
     * Returns the size of the icon and the text with padding.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        Size(PADDING_X + (if (icon != null) ICON + ICON_GAP else 0) + measurer.width(text) + PADDING_X, HEIGHT)

    /**
     * Draws the trigger: raised or underlined while active, with dimmed text while inactive or
     * disabled.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val line = list?.variant == TabsVariant.LINE
        val vertical = tabs?.orientation == Orientation.VERTICAL
        if (active && !line) {
            ui.fillRounded(bounds, ui.tokens.background)
            ui.borderRounded(bounds, ui.tokens.input)
        }
        if (active && line) {
            if (vertical) ui.fill(Rect(bounds.right + 1, bounds.y, 1, bounds.height), ui.tokens.foreground)
            else ui.fill(Rect(bounds.x, bounds.bottom + 1, bounds.width, 1), ui.tokens.foreground)
        }
        val base = if (active || (enabled && isOver(mouseX, mouseY))) ui.tokens.foreground else ui.tokens.mutedForeground
        val color = if (enabled) base else ThemeColors.withAlpha(base, DISABLED_ALPHA)
        val contentWidth = (if (icon != null) ICON + ICON_GAP else 0) + ui.width(text)
        var x = if (vertical) bounds.x + PADDING_X else bounds.x + (bounds.width - contentWidth) / 2
        val y = bounds.y + (bounds.height - ui.lineHeight + 1) / 2
        icon?.let {
            ui.icon(it, Rect(x, bounds.y + (bounds.height - ICON) / 2, ICON, ICON), color)
            x += ICON + ICON_GAP
        }
        ui.text(text, x, y, color)
    }

    /**
     * Focuses the trigger and asks the screen to select its tab on a left click.
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
     * Asks the screen to select the tab on Enter or Space.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled || !isActivation(event)) return false
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
         * The height of a trigger.
         */
        const val HEIGHT: Int = 14

        /**
         * The space left and right of the content.
         */
        const val PADDING_X: Int = 6

        /**
         * The size of the icon.
         */
        const val ICON: Int = 8

        /**
         * The space between the icon and the text.
         */
        const val ICON_GAP: Int = 3

        /**
         * The opacity of a disabled trigger.
         */
        private const val DISABLED_ALPHA: Float = 0.5f
    }
}

/**
 * The content of a tab, shown while its tab is selected.
 *
 * @param id the id of the widget
 * @property value the value of the tab it belongs to
 */
class TabsContentWidget(id: String, val value: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }
}

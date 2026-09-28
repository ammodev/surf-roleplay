package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.Corners
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.PanelSizing
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.Sizing
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent

/**
 * What a widget can ask of the screen that shows it while handling input.
 */
interface UiContext {
    /**
     * The widget that receives keyboard input, or `null` if none does.
     */
    val focusedWidget: Widget?

    /**
     * Gives a widget the keyboard focus, or clears it.
     *
     * @param widget the widget to focus, or `null` to clear the focus
     */
    fun focus(widget: Widget?)

    /**
     * Whether the focus ring is shown: after the player used the keyboard, and not after a mouse
     * click.
     */
    val focusVisible: Boolean get() = true

    /**
     * The open popover, or `null` if none is open.
     */
    val popover: Popover? get() = null

    /**
     * Shows a popover above the screen's content, replacing any open popover.
     *
     * @param popover the popover
     */
    fun openPopover(popover: Popover) = Unit

    /**
     * Closes the open popover, if any.
     */
    fun closePopover() = Unit

    /**
     * The open popovers, from bottom to top.
     */
    val popovers: List<Popover> get() = listOfNotNull(popover)

    /**
     * Closes a popover and every popover opened above it.
     *
     * @param popover the popover
     */
    fun closePopover(popover: Popover) = Unit

    /**
     * Shows a tooltip for the current frame, above everything else.
     *
     * @param json the text as component JSON
     * @param anchor the area the tooltip belongs to
     * @param side the side of the anchor the tooltip is shown on
     */
    fun showTooltip(json: String, anchor: Rect, side: OverlaySide) = Unit

    /**
     * Asks the screen to lay its tree out again before the next frame.
     */
    fun requestLayout()

    /**
     * Reports that the player triggered a widget action, such as clicking a button or pressing a
     * toggle.
     *
     * @param widget the widget
     * @param submitsInput whether the action submits the screen's input, which marks every input
     *        as touched
     */
    fun actionTriggered(widget: Widget, submitsInput: Boolean)

    /**
     * The text on the system clipboard.
     */
    var clipboard: String

    /**
     * Reports that the player changed an input's value.
     *
     * @param widget the input
     * @param immediate whether to report the change at once; otherwise it is reported after the
     *        player paused typing
     */
    fun valueChanged(widget: Widget, immediate: Boolean) = Unit

    /**
     * Reports that the player changed the typed query of a combobox; it is reported after the
     * player paused typing.
     *
     * @param widget the combobox
     * @param query the query
     */
    fun searchChanged(widget: Widget, query: String) = Unit

    /**
     * Finds a widget of the screen by id.
     *
     * @param id the id of the widget
     * @return the widget, or `null` if the screen has none with that id
     */
    fun widget(id: String): Widget? = null
}

/**
 * A node of a roleplay screen's widget tree.
 *
 * A widget describes its layout through [createLayout], receives its [bounds] once the tree is
 * laid out, draws itself and handles the input that reaches it.
 *
 * @property id the id of the widget, unique within its screen
 */
abstract class Widget(val id: String) {
    /**
     * How wide the widget is laid out.
     */
    var width: Sizing = Sizing.FIT

    /**
     * How tall the widget is laid out.
     */
    var height: Sizing = Sizing.FIT

    /**
     * Whether the widget can be used. Disabled widgets ignore input and are drawn dimmed.
     */
    var enabled: Boolean = true

    /**
     * Whether the widget is left out of its container's layout, drawing, input and Tab order,
     * such as a command item that does not match the query. A hidden input still reports its
     * value.
     */
    var hidden: Boolean = false

    /**
     * Whether the mod reports every change of the widget's value at once.
     */
    var notifyChange: Boolean = false

    /**
     * Whether the player changed the widget's value or tried to submit it, after which an invalid
     * value is shown as invalid.
     */
    var touched: Boolean = false

    /**
     * The corners of the widget that are rounded, set by containers that join their children.
     */
    var corners: Corners = Corners.ALL

    /**
     * Whether the widget is drawn without its own fill and border, because a container around it
     * draws them.
     */
    var embedded: Boolean = false

    /**
     * The widget whose bounds the focus ring is drawn around while this widget has the focus, or
     * `null` for this widget itself.
     */
    var focusFrame: Widget? = null

    /**
     * Whether the widget draws its own focus indication, so that the screen draws no focus ring
     * around it.
     */
    open val drawsOwnFocus: Boolean get() = false

    /**
     * Reacts to a click on a label that targets this widget: focuses it if it is enabled.
     *
     * @param context the screen showing the widget
     */
    open fun labelClicked(context: UiContext) {
        if (enabled) context.focus(this)
    }

    /**
     * Whether a check the server made marked the widget as invalid. The mark is cleared when the
     * player changes the widget's value.
     */
    var serverInvalid: Boolean = false

    /**
     * Whether the widget currently shows itself as invalid: while the server marks it invalid.
     */
    open val showsInvalid: Boolean get() = serverInvalid

    /**
     * Records that the player changed the widget's value and reports the change if the widget
     * asked for change events.
     *
     * @param context the screen showing the widget
     * @param immediate whether to report the change at once
     */
    fun markChanged(context: UiContext, immediate: Boolean) {
        touched = true
        serverInvalid = false
        if (notifyChange) context.valueChanged(this, immediate)
    }

    /**
     * The area the widget occupies on screen, set by the last layout.
     */
    var bounds: Rect = Rect.EMPTY
        internal set

    /**
     * The layout box created for the widget by the last [createLayout].
     */
    internal var layoutBox: LayoutBox? = null

    /**
     * Whether a click can give this widget the keyboard focus.
     */
    open val focusable: Boolean get() = false

    /**
     * The child widgets, empty for leaves.
     */
    open val children: List<Widget> get() = emptyList()

    /**
     * The child widgets that take part in the Tab order and in hit testing of the widget's own
     * area: its children, except content that is shown elsewhere, such as the closed content of an
     * overlay.
     */
    open val focusChildren: List<Widget> get() = children

    /**
     * Computes the size of the widget's content, used when it fits its content.
     *
     * @param measurer the text measurer
     * @return the content size
     */
    abstract fun contentSize(measurer: TextMeasurer): Size

    /**
     * Creates the layout box of this widget, and of its children for containers.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    open fun createLayout(measurer: TextMeasurer): LayoutBox =
        LayoutBox(width = width, height = height, content = contentSize(measurer)).also { layoutBox = it }

    /**
     * Creates the layout box of a leaf whose size follows the width it gets, such as a wrapping
     * text.
     *
     * @param measurer the text measurer
     * @param minWidth the narrowest width the widget can take without breaking a word
     * @param measure computes the content size for the largest width the widget may take
     * @return the layout box
     */
    protected fun wrappingLayout(measurer: TextMeasurer, minWidth: Int, measure: (Int) -> Size): LayoutBox =
        LayoutBox(width = width, height = height, content = contentSize(measurer), measureContent = measure, minWidth = minWidth).also { layoutBox = it }

    /**
     * Copies the bounds computed for the layout boxes into this widget and its children.
     */
    open fun applyLayout() {
        bounds = layoutBox?.bounds ?: Rect.EMPTY
    }

    /**
     * Moves the widget and its children by an offset.
     *
     * @param dx the horizontal offset
     * @param dy the vertical offset
     */
    open fun offset(dx: Int, dy: Int) {
        bounds = bounds.copy(x = bounds.x + dx, y = bounds.y + dy)
    }

    /**
     * Draws the widget.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position, or a position outside the screen if the mouse is hidden
     *        from this widget
     * @param mouseY the mouse y position
     */
    abstract fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int)

    /**
     * Handles a mouse click.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was handled
     */
    open fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean = false

    /**
     * Whether a click that focuses this widget starts a drag, after which mouse movement with the
     * button held goes to [mouseDragged].
     */
    open val draggable: Boolean get() = false

    /**
     * Handles mouse movement with the button held after a click on this widget started a drag.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     */
    open fun mouseDragged(context: UiContext, x: Double, y: Double) = Unit

    /**
     * Handles mouse wheel scrolling.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param amount the scroll amount; positive scrolls up
     * @return whether the scrolling was handled
     */
    open fun mouseScrolled(context: UiContext, x: Double, y: Double, amount: Double): Boolean = false

    /**
     * Handles a key press while this widget has the focus.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    open fun keyPressed(context: UiContext, event: KeyEvent): Boolean = false

    /**
     * Handles a typed character while this widget has the focus.
     *
     * @param context the screen showing the widget
     * @param event the character event
     * @return whether the character was handled
     */
    open fun charTyped(context: UiContext, event: CharacterEvent): Boolean = false

    /**
     * The widget's input value in the string form sent to the server, or `null` if the widget is
     * not an input.
     */
    open val inputValue: String? get() = null

    /**
     * Sets the widget's input value from its string form. Widgets that are not inputs ignore it.
     *
     * @param value the value
     */
    open fun applyValue(value: String) = Unit

    /**
     * Sets the widget's text from component JSON. Widgets without text ignore it.
     *
     * @param json the component JSON
     */
    open fun applyText(json: String) = Unit

    /**
     * Checks whether a point is on this widget.
     *
     * @param x the x position
     * @param y the y position
     * @return whether the point lies within [bounds]
     */
    fun isOver(x: Double, y: Double): Boolean = bounds.contains(x, y)

    /**
     * Checks whether a point is on this widget.
     *
     * @param x the x position
     * @param y the y position
     * @return whether the point lies within [bounds]
     */
    fun isOver(x: Int, y: Int): Boolean = bounds.contains(x.toDouble(), y.toDouble())
}

/**
 * A widget that lays out child widgets along an axis.
 *
 * @param id the id of the widget
 * @property axis the layout axis
 */
open class ContainerWidget(id: String, val axis: Axis) : Widget(id) {
    /**
     * The child widgets, in layout order.
     */
    val childList: MutableList<Widget> = mutableListOf()

    /**
     * The child widgets, in layout order.
     */
    override val children: List<Widget> get() = childList

    /**
     * The child widgets that are not hidden, in layout order.
     */
    val shownChildren: List<Widget> get() = childList.filter { !it.hidden }

    /**
     * Only children that are not hidden take part in the Tab order.
     */
    override val focusChildren: List<Widget> get() = shownChildren

    /**
     * The space between two children, in GUI pixels.
     */
    var gap: Int = 0

    /**
     * The space inside the container's edges.
     */
    var padding: Insets = Insets.NONE

    /**
     * How the children are placed along the axis.
     */
    var mainAlign: Align = Align.START

    /**
     * How the children are placed across the axis.
     */
    var crossAlign: Align = Align.START

    /**
     * Whether the children may extend beyond the container along its axis.
     */
    protected open val scrolls: Boolean get() = false

    /**
     * Returns the size of a container's content, which the layout computes from its children.
     *
     * @param measurer the text measurer
     * @return zero
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size.ZERO

    /**
     * Creates the layout box of this container with the boxes of its children.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox = LayoutBox(
        width = width,
        height = height,
        axis = axis,
        children = shownChildren.map { it.createLayout(measurer) },
        gap = gap,
        padding = padding,
        mainAlign = mainAlign,
        crossAlign = crossAlign,
        scrolls = scrolls,
    ).also { layoutBox = it }

    /**
     * Copies the computed bounds into this container and its shown children, and gives hidden
     * children no area.
     */
    override fun applyLayout() {
        super.applyLayout()
        childList.forEach { if (it.hidden) it.bounds = Rect.EMPTY else it.applyLayout() }
    }

    /**
     * Moves the container and its children by an offset.
     *
     * @param dx the horizontal offset
     * @param dy the vertical offset
     */
    override fun offset(dx: Int, dy: Int) {
        super.offset(dx, dy)
        shownChildren.forEach { it.offset(dx, dy) }
    }

    /**
     * Draws every child.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        shownChildren.forEach { it.render(ui, context, mouseX, mouseY) }
    }

    /**
     * Passes a click to the children until one handles it.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether a child handled the click
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean =
        shownChildren.any { it.mouseClicked(context, x, y, button) }

    /**
     * Passes scrolling to the children until one handles it.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param amount the scroll amount
     * @return whether a child handled the scrolling
     */
    override fun mouseScrolled(context: UiContext, x: Double, y: Double, amount: Double): Boolean =
        shownChildren.any { it.mouseScrolled(context, x, y, amount) }
}

/**
 * A vertical container whose children can be taller than itself and are scrolled with the mouse
 * wheel. Children outside the list are clipped and do not receive the mouse.
 *
 * The children are stretched across the list's width, leaving room for the scroll bar at the
 * right.
 *
 * @param id the id of the widget
 */
class ScrollListWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    /**
     * Always `true`: the list scrolls its children.
     */
    override val scrolls: Boolean get() = true

    init {
        crossAlign = Align.STRETCH
        padding = Insets(right = UiMetrics.SCROLL_BAR_WIDTH + 2)
    }

    /**
     * How far the content is scrolled, in GUI pixels from the top.
     */
    var scrollOffset: Int = 0
        private set

    /**
     * The largest scroll offset allowed by the last layout.
     */
    private var maxScroll: Int = 0

    /**
     * Copies the computed bounds into the list and its children, and shifts the children by the
     * scroll offset, clamped to the content.
     */
    override fun applyLayout() {
        super.applyLayout()
        val box = layoutBox ?: return
        maxScroll = (box.contentExtent - bounds.height).coerceAtLeast(0)
        scrollOffset = scrollOffset.coerceIn(0, maxScroll)
        shownChildren.forEach { it.offset(0, -scrollOffset) }
    }

    /**
     * Draws the children clipped to the list, and a scroll bar if the content is taller than the
     * list.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val inside = isOver(mouseX, mouseY)
        ui.clipped(bounds) {
            super.render(ui, context, if (inside) mouseX else HIDDEN, if (inside) mouseY else HIDDEN)
        }
        if (maxScroll > 0) {
            val track = Rect(bounds.right - UiMetrics.SCROLL_BAR_WIDTH, bounds.y, UiMetrics.SCROLL_BAR_WIDTH, bounds.height)
            val content = bounds.height + maxScroll
            val handleHeight = PanelSizing.handleHeight(bounds.height, content)
            val handleY = bounds.y + (bounds.height - handleHeight) * scrollOffset / maxScroll
            ui.fillRounded(Rect(track.x, handleY, track.width, handleHeight), ThemeColors.withAlpha(ui.tokens.mutedForeground, SCROLL_HANDLE_ALPHA), track.width / 2)
        }
    }

    /**
     * Passes a click inside the list to the children.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether a child handled the click
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean =
        isOver(x, y) && super.mouseClicked(context, x, y, button)

    /**
     * Passes scrolling to the children first, and otherwise scrolls the list if the mouse is over
     * it and it can move further in that direction.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param amount the scroll amount; positive scrolls up
     * @return whether a list moved
     */
    override fun mouseScrolled(context: UiContext, x: Double, y: Double, amount: Double): Boolean {
        if (!isOver(x, y)) return false
        if (super.mouseScrolled(context, x, y, amount)) return true
        val next = (scrollOffset - (amount * UiMetrics.SCROLL_STEP).toInt()).coerceIn(0, maxScroll)
        if (next == scrollOffset) return false
        scrollOffset = next
        context.requestLayout()
        return true
    }

    /**
     * Scrolls as little as needed to show a descendant widget inside the list.
     *
     * @param widget the descendant
     * @return whether the list scrolled
     */
    fun ensureVisible(widget: Widget): Boolean {
        val top = widget.bounds.y - bounds.y + scrollOffset
        val bottom = top + widget.bounds.height
        var next = scrollOffset
        if (bottom > next + bounds.height) next = bottom - bounds.height
        if (top < next) next = top
        next = next.coerceIn(0, maxScroll)
        val moved = next != scrollOffset
        scrollOffset = next
        return moved
    }

    /**
     * Holds the hidden mouse position.
     */
    private companion object {
        /**
         * A mouse position that no widget is under.
         */
        const val HIDDEN: Int = Int.MIN_VALUE / 2

        /**
         * The opacity of the scroll bar handle, relative to the muted foreground colour.
         */
        const val SCROLL_HANDLE_ALPHA: Float = 0.4f
    }
}

/**
 * A container that takes the actions of some of its descendants for itself instead of letting
 * them reach the server, such as a collapsible whose trigger button shows its content.
 */
interface ActionInterceptor {
    /**
     * Handles an action of a descendant, or declines it.
     *
     * @param context the screen showing the widget
     * @param widget the widget whose action fired
     * @param via the child of this container that holds the widget, or the widget itself
     * @return whether the action was handled and must not reach the server
     */
    fun interceptAction(context: UiContext, widget: Widget, via: Widget): Boolean
}

/**
 * A container that handles the keys its focused descendants do not use, such as a tab list that
 * moves between its triggers on the arrow keys.
 */
interface KeyInterceptor {
    /**
     * Handles a key the focused descendant did not use, or declines it.
     *
     * @param context the screen showing the widget
     * @param focused the focused descendant
     * @param event the key event
     * @return whether the key was handled
     */
    fun descendantKeyPressed(context: UiContext, focused: Widget, event: KeyEvent): Boolean
}

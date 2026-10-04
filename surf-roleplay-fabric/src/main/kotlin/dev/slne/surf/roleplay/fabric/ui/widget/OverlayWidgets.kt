package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.OverlayPlacement
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.OverlaySide

/**
 * The content of an overlay: a stack of widgets that is laid out and drawn by the overlay while
 * it is open, and hidden otherwise.
 *
 * @param id the id of the widget
 */
open class OverlayContentWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }
}

/**
 * A widget that owns an overlay, such as a popover or a menu. Its children are its triggers and
 * one [OverlayContentWidget]. Only the triggers are laid out, drawn and reached by the mouse and
 * the Tab order in place; the content is shown by the overlay while it is open. The overlay's open
 * state is the widget's input value, `true` or `false`.
 *
 * @param id the id of the widget
 * @param axis the axis the triggers are laid out along
 */
abstract class OverlayHostWidget(id: String, axis: Axis = Axis.HORIZONTAL) : ContainerWidget(id, axis) {
    init {
        crossAlign = if (axis == Axis.HORIZONTAL) Align.CENTER else Align.STRETCH
    }

    /**
     * The content of the overlay, or `null` if the host has none.
     */
    val content: OverlayContentWidget? get() = childList.firstOrNull { it is OverlayContentWidget } as OverlayContentWidget?

    /**
     * The triggers: every child but the content.
     */
    val triggers: List<Widget> get() = childList.filter { it !is OverlayContentWidget && !it.hidden }

    /**
     * Whether the overlay is open.
     */
    var open: Boolean = false
        private set

    /**
     * The open state the server asked for, applied at the next layout, or `null` if it asked for
     * nothing.
     */
    private var requested: Boolean? = null

    /**
     * Only the triggers take part in the Tab order in place.
     */
    override val focusChildren: List<Widget> get() = triggers

    /**
     * The open state as `true` or `false`.
     */
    override val inputValue: String get() = open.toString()

    /**
     * Asks for the overlay to be opened or closed at the next layout, for a value set by the
     * server.
     *
     * @param value `true` to open the overlay, anything else to close it
     */
    override fun applyValue(value: String) = requestOpen(value == "true")

    /**
     * Asks for the overlay to be opened or closed at the next layout, without reporting the change
     * as the player's.
     *
     * @param open whether the overlay should be open
     */
    fun requestOpen(open: Boolean) {
        requested = open
    }

    /**
     * Whether an action of a widget inside the triggers opens or closes the overlay instead of
     * reaching the server. Hosts that open on a right click or on hover keep their triggers'
     * actions.
     */
    open val togglesOnTriggerAction: Boolean get() = true

    /**
     * Creates the overlay this host shows.
     *
     * @return the overlay
     */
    abstract fun createPopover(): Popover

    /**
     * The area the overlay is placed next to: the area of the triggers.
     *
     * @return the anchor area
     */
    open fun anchor(): Rect = bounds

    /**
     * Opens the overlay if it is closed and closes it otherwise, reporting the change as the
     * player's.
     *
     * @param context the screen showing the host
     */
    fun toggle(context: UiContext) {
        if (open) close(context) else show(context, report = true)
    }

    /**
     * Opens the overlay if it is not open.
     *
     * @param context the screen showing the host
     * @param report whether to report the change as the player's
     */
    fun show(context: UiContext, report: Boolean) {
        if (open) return
        context.openPopover(createPopover())
        open = true
        if (report) markChanged(context, immediate = true)
    }

    /**
     * Closes the overlay if it is open, together with every overlay opened above it.
     *
     * @param context the screen showing the host
     */
    fun close(context: UiContext) {
        context.popovers.firstOrNull { it.owner === this }?.let { context.closePopover(it) }
    }

    /**
     * Records that the overlay was closed and reports the change as the player's, unless the
     * server asked for it.
     *
     * @param context the screen that showed the overlay
     */
    fun overlayClosed(context: UiContext) {
        if (!open) return
        open = false
        if (requested == false) requested = null else markChanged(context, immediate = true)
    }

    /**
     * Applies the open state the server asked for.
     *
     * @param context the screen showing the host
     */
    fun sync(context: UiContext) {
        val wanted = requested ?: return
        if (wanted) {
            requested = null
            show(context, report = false)
        } else if (open) {
            close(context)
        } else {
            requested = null
        }
    }

    /**
     * Creates the layout box of the triggers.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox = LayoutBox(
        width = width,
        height = height,
        axis = axis,
        children = triggers.map { it.createLayout(measurer) },
        gap = gap,
        padding = padding,
        mainAlign = mainAlign,
        crossAlign = crossAlign,
    ).also { layoutBox = it }

    /**
     * Copies the computed bounds into the host and its triggers.
     */
    override fun applyLayout() {
        bounds = layoutBox?.bounds ?: Rect.EMPTY
        triggers.forEach { it.applyLayout() }
    }

    /**
     * Moves the host and its triggers by an offset.
     *
     * @param dx the horizontal offset
     * @param dy the vertical offset
     */
    override fun offset(dx: Int, dy: Int) {
        bounds = bounds.copy(x = bounds.x + dx, y = bounds.y + dy)
        triggers.forEach { it.offset(dx, dy) }
    }

    /**
     * Draws the triggers that reach into the current clip, and lets the others keep their state
     * through [renderSkipped].
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        renderVisible(triggers, ui, context, mouseX, mouseY)
    }

    /**
     * Passes the skipped frame on to the triggers; the content is drawn by the overlay.
     *
     * @param context the screen showing the widget
     */
    override fun renderSkipped(context: UiContext) {
        triggers.forEach { it.renderSkipped(context) }
    }

    /**
     * Passes a click to the triggers.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether a trigger handled the click
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean =
        triggers.any { it.mouseClicked(context, x, y, button) }

    /**
     * Passes scrolling to the triggers.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param amount the scroll amount
     * @return whether a trigger handled the scrolling
     */
    override fun mouseScrolled(context: UiContext, x: Double, y: Double, amount: Double): Boolean =
        triggers.any { it.mouseScrolled(context, x, y, amount) }

    /**
     * Finds overlay hosts around widgets.
     */
    companion object {
        /**
         * Finds the host whose trigger a widget is part of.
         *
         * @param root the root of the tree
         * @param widget the widget
         * @return the host, or `null` if the widget is not inside a trigger of any host whose
         *         triggers toggle it
         */
        fun hostOfTrigger(root: Widget, widget: Widget): OverlayHostWidget? {
            var current = widget
            while (true) {
                val parent = WidgetTree.parentOf(root, current.id) ?: return null
                if (parent is OverlayHostWidget) return if (current is OverlayContentWidget || !parent.togglesOnTriggerAction) null else parent
                current = parent
            }
        }
    }
}

/**
 * An overlay that shows the content of its host next to the host's anchor.
 *
 * @property owner the host that opened the overlay
 * @property side the side of the anchor the content opens on
 * @property align how the content is aligned along that side
 * @property offset the space between the anchor and the content
 * @property modal whether everything below the overlay is dimmed and receives no input
 * @property dismissOnOutsideClick whether a click outside a modal overlay closes it
 * @property trapFocus whether Tab cycles through the content while the overlay is open
 */
open class WidgetPopover(
    override val owner: OverlayHostWidget,
    val side: OverlaySide,
    val align: Align,
    val offset: Int = OverlayPlacement.DEFAULT_OFFSET,
    override val modal: Boolean = false,
    override val dismissOnOutsideClick: Boolean = true,
    val trapFocus: Boolean = modal,
) : Popover {

    /**
     * The content shown, read from the host so that patches of the content take effect.
     */
    val content: OverlayContentWidget? get() = owner.content

    /**
     * The widget Tab cycles through while the overlay traps the focus.
     */
    override val focusRoot: Widget? get() = if (trapFocus) content else null

    /**
     * Computes the area the content is placed in, given its size. When the host is
     * right-to-left, left and right swap, and so do start and end alignment above and below the
     * anchor.
     *
     * @param size the size of the content
     * @param window the window area
     * @return the area
     */
    open fun place(size: dev.slne.surf.roleplay.fabric.ui.layout.Size, window: Rect): Rect {
        val rtl = owner.rtl
        val across = side == OverlaySide.TOP || side == OverlaySide.BOTTOM
        val shownAlign = when {
            !rtl || !across -> align
            align == Align.START -> Align.END
            align == Align.END -> Align.START
            else -> align
        }
        return OverlayPlacement.place(owner.anchor(), size, window, side.inDirection(rtl), shownAlign, offset)
    }

    /**
     * Returns the largest width the content may take.
     *
     * @param window the window area
     * @return the width
     */
    open fun maxWidth(window: Rect): Int = window.width - 2 * OverlayPlacement.WINDOW_MARGIN

    /**
     * Lays the content out at its size, placed next to the anchor, and returns its area.
     *
     * @param window the window area
     * @param measurer the text measurer
     * @return the content's area
     */
    override fun area(window: Rect, measurer: TextMeasurer): Rect {
        val shown = content ?: return Rect.EMPTY
        val box = shown.createLayout(measurer)
        val area = place(FlexLayout.measure(box, maxWidth(window)), window)
        FlexLayout.layout(box, area)
        shown.applyLayout()
        return area
    }

    /**
     * Draws the content, with the focus ring of a focused widget inside it.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the overlay
     * @param area the overlay's area
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, area: Rect, mouseX: Int, mouseY: Int) {
        val shown = content ?: return
        shown.render(ui, context, mouseX, mouseY)
        context.focusedWidget?.takeIf { context.focusVisible && !it.drawsOwnFocus && containsWidget(it) }?.let { focused ->
            val b = (focused.focusFrame ?: focused).bounds
            ui.borderRounded(Rect(b.x - 1, b.y - 1, b.width + 2, b.height + 2), ui.tokens.ring, ui.tokens.radius + 1)
        }
    }

    /**
     * Passes a click to the content.
     *
     * @param context the screen showing the overlay
     * @param area the overlay's area
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     */
    override fun mouseClicked(context: UiContext, area: Rect, x: Double, y: Double, button: Int) {
        content?.mouseClicked(context, x, y, button)
    }

    /**
     * Passes scrolling to the content.
     *
     * @param context the screen showing the overlay
     * @param area the overlay's area
     * @param x the mouse x position
     * @param y the mouse y position
     * @param amount the scroll amount
     */
    override fun mouseScrolled(context: UiContext, area: Rect, x: Double, y: Double, amount: Double) {
        content?.mouseScrolled(context, x, y, amount)
    }

    /**
     * Leaves keys to the focused widget.
     *
     * @param context the screen showing the overlay
     * @param event the key event
     * @return `false`
     */
    override fun keyPressed(context: UiContext, event: net.minecraft.client.input.KeyEvent): Boolean = false

    /**
     * Checks whether a widget is part of the content.
     *
     * @param widget the widget
     * @return whether the widget is inside the content
     */
    override fun containsWidget(widget: Widget): Boolean = content?.let { WidgetTree.find(it, widget.id) === widget } ?: false

    /**
     * Tells the host that its overlay was closed.
     *
     * @param context the screen that showed the overlay
     */
    override fun closed(context: UiContext) = owner.overlayClosed(context)
}

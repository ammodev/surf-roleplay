package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelSizing
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ScrollOrientation
import org.lwjgl.glfw.GLFW

/**
 * A container that scrolls a descendant into view when it takes the focus.
 */
interface ScrollContainer {
    /**
     * Scrolls as little as needed to show a descendant.
     *
     * @param widget the descendant
     * @return whether the container scrolled
     */
    fun ensureVisible(widget: Widget): Boolean
}

/**
 * A scroll area: its children, stacked, laid out as wide or tall as they need along the directions
 * the area scrolls in, and scrolled inside the area with the mouse wheel or by dragging thin
 * scroll bars drawn over the content. Content outside the area is clipped and does not receive
 * the mouse.
 *
 * In a vertical area the content is as wide as the area; in a horizontal one it is as tall as the
 * area. The wheel scrolls vertically, and horizontally when the area only scrolls horizontally or
 * cannot move further vertically.
 *
 * @param id the id of the widget
 * @property orientation the directions the area scrolls in
 */
class ScrollAreaWidget(id: String, val orientation: ScrollOrientation) : ContainerWidget(id, Axis.VERTICAL), ScrollContainer {
    init {
        crossAlign = Align.STRETCH
    }

    /**
     * How far the content is scrolled from the left.
     */
    var scrollX: Int = 0
        private set

    /**
     * How far the content is scrolled from the top.
     */
    var scrollY: Int = 0
        private set

    /**
     * The largest horizontal scroll offset allowed by the last layout.
     */
    var maxScrollX: Int = 0
        private set

    /**
     * The largest vertical scroll offset allowed by the last layout.
     */
    var maxScrollY: Int = 0
        private set

    /**
     * The layout box of the content, laid out inside the area at the last layout.
     */
    private var contentBox: LayoutBox? = null

    /**
     * The bar being dragged and where on its thumb it was grabbed, or `null` while none is.
     */
    private var grab: Pair<Axis, Int>? = null

    /**
     * Whether the area scrolls horizontally.
     */
    private val scrollsX: Boolean get() = orientation != ScrollOrientation.VERTICAL

    /**
     * Whether the area scrolls vertically.
     */
    private val scrollsY: Boolean get() = orientation != ScrollOrientation.HORIZONTAL

    /**
     * Creates a leaf layout box for the area, sized by its own sizing or by its content, and
     * keeps the box of the content to lay it out separately.
     *
     * @param measurer the text measurer
     * @return the layout box of the area
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        val content = LayoutBox(
            axis = Axis.VERTICAL,
            children = shownChildren.map { it.createLayout(measurer) },
            gap = gap,
            padding = padding,
            crossAlign = Align.STRETCH,
        )
        contentBox = content
        return LayoutBox(
            width = width,
            height = height,
            measureContent = { limit -> FlexLayout.measure(content, if (scrollsX) FlexLayout.UNBOUNDED else limit) },
        ).also { layoutBox = it }
    }

    /**
     * Lays the content out at the size it needs, at least the size of the area, and shifts it by
     * the scroll offsets, clamped to the content.
     */
    override fun applyLayout() {
        bounds = layoutBox?.bounds ?: Rect.EMPTY
        val content = contentBox ?: return
        val width = if (scrollsX) FlexLayout.measure(content).width.coerceAtLeast(bounds.width) else bounds.width
        val height = FlexLayout.measureAt(content, width).height.let { if (scrollsY) it.coerceAtLeast(bounds.height) else bounds.height }
        FlexLayout.layout(content, Rect(bounds.x, bounds.y, width, height))
        maxScrollX = (width - bounds.width).coerceAtLeast(0)
        maxScrollY = (height - bounds.height).coerceAtLeast(0)
        scrollX = scrollX.coerceIn(0, maxScrollX)
        scrollY = scrollY.coerceIn(0, maxScrollY)
        childList.forEach { if (it.hidden) it.bounds = Rect.EMPTY else it.applyLayout() }
        shownChildren.forEach { it.offset(-scrollX, -scrollY) }
    }

    /**
     * Returns the area of the thumb of the vertical bar.
     *
     * @return the thumb, or an empty area if the area cannot scroll vertically
     */
    fun verticalThumb(): Rect {
        if (maxScrollY <= 0) return Rect.EMPTY
        val length = PanelSizing.handleHeight(bounds.height, bounds.height + maxScrollY)
        val y = bounds.y + (bounds.height - length) * scrollY / maxScrollY
        return Rect(bounds.right - BAR, y, BAR, length)
    }

    /**
     * Returns the area of the thumb of the horizontal bar.
     *
     * @return the thumb, or an empty area if the area cannot scroll horizontally
     */
    fun horizontalThumb(): Rect {
        if (maxScrollX <= 0) return Rect.EMPTY
        val length = PanelSizing.handleHeight(bounds.width, bounds.width + maxScrollX)
        val x = bounds.x + (bounds.width - length) * scrollX / maxScrollX
        return Rect(x, bounds.bottom - BAR, length, BAR)
    }

    /**
     * Draws the content clipped to the area, and the thumbs of the bars that can scroll.
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
        listOf(verticalThumb(), horizontalThumb()).filter { it.width > 0 }.forEach { thumb ->
            ui.fillRounded(Rect(thumb.x + 1, thumb.y + 1, thumb.width - 2, thumb.height - 2), ui.tokens.border, BAR / 2)
        }
    }

    /**
     * Grabs a thumb under the mouse for dragging, and otherwise passes a click inside the area to
     * the content.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was inside the area
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            val vertical = verticalThumb()
            val horizontal = horizontalThumb()
            grab = when {
                vertical.contains(x, y) -> Axis.VERTICAL to (y.toInt() - vertical.y)
                horizontal.contains(x, y) -> Axis.HORIZONTAL to (x.toInt() - horizontal.x)
                else -> null
            }
            if (grab != null) {
                context.beginDrag(this)
                return true
            }
        }
        super.mouseClicked(context, x, y, button)
        return true
    }

    /**
     * Whether the area can be dragged: while a thumb is grabbed.
     */
    override val draggable: Boolean get() = grab != null

    /**
     * Moves the grabbed thumb to follow the mouse and scrolls the content in proportion.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     */
    override fun mouseDragged(context: UiContext, x: Double, y: Double) {
        val (axis, offset) = grab ?: return
        if (axis == Axis.VERTICAL) {
            val thumb = verticalThumb()
            val track = bounds.height - thumb.height
            if (track > 0) scrollY = ((y.toInt() - offset - bounds.y) * maxScrollY / track).coerceIn(0, maxScrollY)
        } else {
            val thumb = horizontalThumb()
            val track = bounds.width - thumb.width
            if (track > 0) scrollX = ((x.toInt() - offset - bounds.x) * maxScrollX / track).coerceIn(0, maxScrollX)
        }
        context.requestLayout()
    }

    /**
     * Passes scrolling to the content first, and otherwise scrolls the area if the mouse is over
     * it and it can move further.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param amount the scroll amount; positive scrolls up or left
     * @return whether the area or its content moved
     */
    override fun mouseScrolled(context: UiContext, x: Double, y: Double, amount: Double): Boolean {
        if (!isOver(x, y)) return false
        if (super.mouseScrolled(context, x, y, amount)) return true
        val step = (amount * UiMetrics.SCROLL_STEP).toInt()
        if (scrollsY) {
            val next = (scrollY - step).coerceIn(0, maxScrollY)
            if (next != scrollY) {
                scrollY = next
                context.requestLayout()
                return true
            }
        }
        if (scrollsX) {
            val next = (scrollX - step).coerceIn(0, maxScrollX)
            if (next != scrollX) {
                scrollX = next
                context.requestLayout()
                return true
            }
        }
        return false
    }

    /**
     * Scrolls as little as needed to show a descendant inside the area.
     *
     * @param widget the descendant
     * @return whether the area scrolled
     */
    override fun ensureVisible(widget: Widget): Boolean {
        val beforeX = scrollX
        val beforeY = scrollY
        val left = widget.bounds.x - bounds.x + scrollX
        val top = widget.bounds.y - bounds.y + scrollY
        if (left + widget.bounds.width > scrollX + bounds.width) scrollX = left + widget.bounds.width - bounds.width
        if (left < scrollX) scrollX = left
        if (top + widget.bounds.height > scrollY + bounds.height) scrollY = top + widget.bounds.height - bounds.height
        if (top < scrollY) scrollY = top
        scrollX = scrollX.coerceIn(0, maxScrollX)
        scrollY = scrollY.coerceIn(0, maxScrollY)
        return scrollX != beforeX || scrollY != beforeY
    }

    /**
     * Holds the scroll area metrics.
     */
    companion object {
        /**
         * The thickness of a scroll bar.
         */
        const val BAR: Int = 5

        /**
         * A mouse position that no widget is under.
         */
        private const val HIDDEN: Int = Int.MIN_VALUE / 2
    }
}

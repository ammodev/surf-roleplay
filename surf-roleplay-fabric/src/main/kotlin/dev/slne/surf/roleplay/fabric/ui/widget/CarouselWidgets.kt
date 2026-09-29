package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ButtonVariant
import dev.slne.surf.roleplay.protocol.screen.Orientation
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

/**
 * A carousel: its previous button, its content of slides and its next button, in a row or, for a
 * vertical carousel, a column. The index of the first shown slide is its input value. The
 * buttons and the Left and Right keys move it by one slide; without loop it stops at the first
 * slide and at the last position that still fills the content. Right-to-left, the previous button
 * is on the right and the slides run from the right.
 *
 * @param id the id of the widget
 * @property orientation whether the slides move sideways or up and down
 * @property loop whether the last position is followed by the first
 */
class CarouselWidget(id: String, val orientation: Orientation, val loop: Boolean) :
    ContainerWidget(id, if (orientation == Orientation.HORIZONTAL) Axis.HORIZONTAL else Axis.VERTICAL),
    ActionInterceptor,
    KeyInterceptor {

    init {
        gap = GAP
        crossAlign = Align.CENTER
    }

    /**
     * The index of the first shown slide.
     */
    var index: Int = 0
        private set

    /**
     * The content of the carousel, or `null` if it has none.
     */
    val content: CarouselContentWidget? get() = childList.firstOrNull { it is CarouselContentWidget } as CarouselContentWidget?

    /**
     * The index of the first shown slide.
     */
    override val inputValue: String get() = index.toString()

    /**
     * Shows the slide the server names, within the slides, without reporting the change.
     *
     * @param value the index of the slide
     */
    override fun applyValue(value: String) {
        value.trim().toIntOrNull()?.let { show(it) }
    }

    /**
     * The largest index the carousel can show: the first slide from which the remaining slides no
     * longer fill the content.
     */
    val maxIndex: Int
        get() {
            val items = content?.items ?: return 0
            var rest = 0.0
            for (i in items.indices.reversed()) {
                rest += items[i].basis
                if (rest > FULL + EPSILON) return (i + 1).coerceAtMost(items.lastIndex)
            }
            return 0
        }

    /**
     * Shows a slide, clamped to the positions the carousel can show, and enables the buttons that
     * can move further.
     *
     * @param index the index of the slide
     */
    fun show(index: Int) {
        this.index = index.coerceIn(0, maxIndex)
        content?.index = this.index
        childList.filterIsInstance<CarouselButtonWidget>().forEach { button ->
            button.enabled = button.usable && (loop || if (button.next) this.index < maxIndex else this.index > 0)
        }
    }

    /**
     * Moves the carousel by a number of slides, wrapping around if it loops, and reports the
     * change.
     *
     * @param context the screen showing the widget
     * @param delta the number of slides; negative moves back
     */
    fun move(context: UiContext, delta: Int) {
        val max = maxIndex
        val target = index + delta
        val next = when {
            loop && target < 0 -> max
            loop && target > max -> 0
            else -> target.coerceIn(0, max)
        }
        if (next == index) return
        show(next)
        markChanged(context, immediate = true)
        context.requestLayout()
    }

    /**
     * Moves the carousel when its previous or next button fires.
     *
     * @param context the screen showing the widget
     * @param widget the widget whose action fired
     * @param via the child holding the widget
     * @return whether the action came from a carousel button
     */
    override fun interceptAction(context: UiContext, widget: Widget, via: Widget): Boolean {
        if (widget !is CarouselButtonWidget || via !== widget) return false
        if (enabled) move(context, if (widget.next) 1 else -1)
        return true
    }

    /**
     * Moves the carousel on Left and Right while a widget inside it has the focus: the key towards
     * the previous button moves back and the other one forward, so Left moves forward when
     * right-to-left.
     *
     * @param context the screen showing the widget
     * @param focused the focused widget
     * @param event the key event
     * @return whether the key moved the carousel
     */
    override fun descendantKeyPressed(context: UiContext, focused: Widget, event: KeyEvent): Boolean {
        val back = if (rtl) GLFW.GLFW_KEY_RIGHT else GLFW.GLFW_KEY_LEFT
        val delta = when (event.key()) {
            back -> -1
            GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT -> 1
            else -> return false
        }
        if (enabled) move(context, delta)
        return true
    }

    /**
     * Holds the carousel metrics.
     */
    companion object {
        /**
         * The space between the buttons and the content.
         */
        const val GAP: Int = 4

        /**
         * The share of the content that is filled, in percent.
         */
        private const val FULL: Double = 100.0

        /**
         * How far a share may miss through rounding.
         */
        private const val EPSILON: Double = 0.01
    }
}

/**
 * The content of a carousel: its slides in a row or a column, each at its share of the content,
 * shifted so that the current slide starts at the start of the content. Slides outside the
 * content are clipped and do not receive the mouse.
 *
 * @param id the id of the widget
 * @property orientation whether the slides are in a row or a column
 */
class CarouselContentWidget(id: String, val orientation: Orientation) : ContainerWidget(id, if (orientation == Orientation.HORIZONTAL) Axis.HORIZONTAL else Axis.VERTICAL) {

    /**
     * The slides, in order.
     */
    val items: List<CarouselItemWidget> get() = childList.filterIsInstance<CarouselItemWidget>()

    /**
     * The index of the slide shown at the start of the content.
     */
    var index: Int = 0

    /**
     * Creates a leaf layout box for the content, as tall (or, when vertical, as wide) as its
     * tallest slide at its share, and keeps the boxes of the slides.
     *
     * @param measurer the text measurer
     * @return the layout box of the content
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        val shown = items.filter { !it.hidden }
        val boxes = shown.map { it.createLayout(measurer) }
        return LayoutBox(
            width = width,
            height = height,
            measureContent = { limit ->
                if (orientation == Orientation.HORIZONTAL) {
                    val width = if (limit < FlexLayout.UNBOUNDED) limit else boxes.maxOfOrNull { FlexLayout.measure(it).width } ?: 0
                    val height = shown.indices.maxOfOrNull { FlexLayout.measureAt(boxes[it], (shown[it].basis * width / 100.0).roundToInt()).height } ?: 0
                    Size(width, height)
                } else {
                    val width = boxes.maxOfOrNull { FlexLayout.measure(it, limit).width } ?: 0
                    Size(width, boxes.sumOf { FlexLayout.measureAt(it, width).height })
                }
            },
        ).also { layoutBox = it }
    }

    /**
     * Lays the slides out at their shares along the content and shifts them so that the current
     * slide starts at the start of the content, which is its right edge when horizontal and
     * right-to-left.
     */
    override fun applyLayout() {
        bounds = layoutBox?.bounds ?: Rect.EMPTY
        val horizontal = orientation == Orientation.HORIZONTAL
        val main = if (horizontal) bounds.width else bounds.height
        val shown = items.filter { !it.hidden }
        val lengths = shown.map { (it.basis * main / 100.0).roundToInt() }
        var cursor = (if (horizontal) bounds.x else bounds.y) - lengths.take(index.coerceIn(0, lengths.size)).sum()
        shown.forEachIndexed { i, item ->
            val area = if (horizontal) mirrored(Rect(cursor, bounds.y, lengths[i], bounds.height)) else Rect(bounds.x, cursor, bounds.width, lengths[i])
            item.layoutBox?.let { FlexLayout.layout(it, area) }
            item.applyLayout()
            cursor += lengths[i]
        }
        childList.filter { it.hidden }.forEach { it.bounds = Rect.EMPTY }
    }

    /**
     * Draws the slides clipped to the content.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val inside = isOver(mouseX, mouseY)
        ui.clipped(bounds) { super.render(ui, context, if (inside) mouseX else HIDDEN, if (inside) mouseY else HIDDEN) }
    }

    /**
     * Passes a click inside the content to the slides.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether a slide handled the click
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean =
        isOver(x, y) && super.mouseClicked(context, x, y, button)

    /**
     * Holds the hidden mouse position.
     */
    private companion object {
        /**
         * A mouse position that no widget is under.
         */
        const val HIDDEN: Int = Int.MIN_VALUE / 2
    }
}

/**
 * A slide of a carousel: its content, stacked, at its share of the carousel content.
 *
 * @param id the id of the widget
 * @property basis the share of the content the slide takes, in percent
 */
class CarouselItemWidget(id: String, val basis: Double) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }
}

/**
 * The previous or next button of a carousel: a round outline button with an arrow, pointing up
 * or down in a vertical carousel. Right-to-left, the arrows of a horizontal carousel point the
 * other way.
 *
 * @param id the id of the widget
 * @property next whether the button shows the next slide rather than the previous one
 * @property usable whether the server lets the button be used
 * @property vertical whether the carousel moves up and down
 */
class CarouselButtonWidget(id: String, val next: Boolean, val usable: Boolean, val vertical: Boolean) : ClickableWidget(id) {

    /**
     * Returns the size of the round button.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(SIZE, SIZE)

    /**
     * Draws the round outline button and its arrow, dimmed while disabled.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val colors = ButtonStyle.colors(ui.tokens, ButtonVariant.OUTLINE, enabled && isOver(mouseX, mouseY))
        val fade: (Int) -> Int = { if (enabled) it else ui.disabled(it) }
        colors.background?.let { ui.fillRounded(bounds, fade(it), SIZE / 2) }
        colors.border?.let { ui.borderRounded(bounds, fade(it), SIZE / 2) }
        val icon = Rect(bounds.x + (SIZE - ICON) / 2, bounds.y + (SIZE - ICON) / 2, ICON, ICON)
        ui.rotatedIcon(directionalIcon(if (next) "arrow-right" else "arrow-left", rtl && !vertical), icon, fade(colors.foreground), if (vertical) 90f else 0f)
    }

    /**
     * Holds the button metrics.
     */
    companion object {
        /**
         * The width and height of the button.
         */
        const val SIZE: Int = 16

        /**
         * The size of the arrow.
         */
        const val ICON: Int = 8
    }
}

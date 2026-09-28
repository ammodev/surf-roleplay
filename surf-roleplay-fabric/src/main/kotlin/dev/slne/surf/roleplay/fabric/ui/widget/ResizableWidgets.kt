package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.Orientation
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

/**
 * A group of panels side by side or stacked, with handles between them. Each panel takes a share
 * of the group in percent; the shares are the group's input value, comma separated, in the order
 * of the panels. The handles take their own thickness, and the panels share the rest.
 *
 * @param id the id of the widget
 * @property orientation whether the panels are side by side or stacked
 */
class ResizablePanelGroupWidget(id: String, val orientation: Orientation) : ContainerWidget(id, if (orientation == Orientation.HORIZONTAL) Axis.HORIZONTAL else Axis.VERTICAL) {

    /**
     * The panels, in order.
     */
    val panels: List<ResizablePanelWidget> get() = childList.filterIsInstance<ResizablePanelWidget>()

    /**
     * The share of every panel, in percent, in the order of the panels.
     */
    private var sizes: MutableList<Double> = mutableListOf()

    /**
     * The shares of the panels in percent, comma separated, with at most one decimal.
     */
    override val inputValue: String get() = sizes.joinToString(",") { format(it) }

    /**
     * Applies the shares the server sets, each clamped to its panel's limits, if it names one
     * share per panel; other values are ignored.
     *
     * @param value the shares in percent, comma separated
     */
    override fun applyValue(value: String) {
        val parsed = value.split(',').map { it.trim().toDoubleOrNull() ?: return }
        if (parsed.size != panels.size) return
        sizes = parsed.mapIndexed { index, size -> size.coerceIn(panels[index].minSize, panels[index].maxSize) }.toMutableList()
    }

    /**
     * Sets the shares from the default sizes of the panels: panels without a default share what
     * the others leave equally.
     */
    fun resetSizes() {
        val panels = panels
        val fixed = panels.filter { it.defaultSize > 0.0 }.sumOf { it.defaultSize }
        val open = panels.count { it.defaultSize <= 0.0 }
        val rest = if (open > 0) ((100.0 - fixed) / open).coerceAtLeast(0.0) else 0.0
        sizes = panels.map { (if (it.defaultSize > 0.0) it.defaultSize else rest).coerceIn(it.minSize, it.maxSize) }.toMutableList()
    }

    /**
     * Creates a leaf layout box for the group, sized by its own sizing or by the panels and
     * handles side by side, and keeps the boxes of the children to lay them out by their shares.
     *
     * @param measurer the text measurer
     * @return the layout box of the group
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        val boxes = shownChildren.map { it.createLayout(measurer) }
        return LayoutBox(
            width = width,
            height = height,
            measureContent = { limit ->
                val sizes = boxes.map { FlexLayout.measure(it, limit) }
                if (axis == Axis.HORIZONTAL) Size(sizes.sumOf { it.width }, sizes.maxOfOrNull { it.height } ?: 0)
                else Size(sizes.maxOfOrNull { it.width } ?: 0, sizes.sumOf { it.height })
            },
        ).also { layoutBox = it }
    }

    /**
     * The length along the axis that the panels share: the group without its handles.
     */
    val available: Int
        get() = ((if (axis == Axis.HORIZONTAL) bounds.width else bounds.height) - childList.filterIsInstance<ResizableHandleWidget>().sumOf { HANDLE }).coerceAtLeast(0)

    /**
     * Lays the panels out by their shares and the handles between them; the last panel takes
     * what rounding leaves.
     */
    override fun applyLayout() {
        bounds = layoutBox?.bounds ?: Rect.EMPTY
        if (sizes.size != panels.size) resetSizes()
        val total = available
        var cursor = if (axis == Axis.HORIZONTAL) bounds.x else bounds.y
        val end = if (axis == Axis.HORIZONTAL) bounds.right else bounds.bottom
        val shown = childList
        val lastPanel = panels.lastOrNull()
        var panelIndex = 0
        shown.forEach { child ->
            val length = when (child) {
                is ResizableHandleWidget -> HANDLE
                is ResizablePanelWidget -> if (child === lastPanel) (end - cursor).coerceAtLeast(0) else (sizes[panelIndex++] * total / 100.0).roundToInt()
                else -> 0
            }
            val area = if (axis == Axis.HORIZONTAL) Rect(cursor, bounds.y, length, bounds.height) else Rect(bounds.x, cursor, bounds.width, length)
            child.layoutBox?.let { FlexLayout.layout(it, area) }
            child.applyLayout()
            cursor += length
        }
    }

    /**
     * Moves the boundary at a handle so that the panel before it ends at a position, within the
     * limits of the panels on both sides.
     *
     * @param handle the handle
     * @param position the position along the axis the panel before the handle should end at
     * @return whether the shares changed
     */
    fun moveTo(handle: ResizableHandleWidget, position: Int): Boolean {
        val before = beforeIndex(handle) ?: return false
        val panel = panels[before]
        val start = if (axis == Axis.HORIZONTAL) panel.bounds.x else panel.bounds.y
        val total = available.takeIf { it > 0 } ?: return false
        return resize(before, (position - start) * 100.0 / total - sizes[before])
    }

    /**
     * Grows the panel before a handle by a number of percent and shrinks the panel after it by
     * the same, within the limits of both.
     *
     * @param handle the handle
     * @param delta the change in percent; negative shrinks the panel before the handle
     * @return whether the shares changed
     */
    fun moveBy(handle: ResizableHandleWidget, delta: Double): Boolean {
        val before = beforeIndex(handle) ?: return false
        return resize(before, delta)
    }

    /**
     * Grows a panel and shrinks the next one by a number of percent, clamped to the limits of
     * both.
     *
     * @param index the index of the panel before the handle
     * @param delta the change in percent
     * @return whether the shares changed
     */
    private fun resize(index: Int, delta: Double): Boolean {
        if (index + 1 >= sizes.size) return false
        val a = panels[index]
        val b = panels[index + 1]
        val low = maxOf(a.minSize - sizes[index], sizes[index + 1] - b.maxSize)
        val high = minOf(a.maxSize - sizes[index], sizes[index + 1] - b.minSize)
        val applied = delta.coerceIn(minOf(low, 0.0), maxOf(high, 0.0))
        if (applied == 0.0) return false
        sizes[index] = round(sizes[index] + applied)
        sizes[index + 1] = round(sizes[index + 1] - applied)
        return true
    }

    /**
     * Returns the index of the panel before a handle.
     *
     * @param handle the handle
     * @return the index, or `null` if the handle has no panel before it
     */
    private fun beforeIndex(handle: ResizableHandleWidget): Int? {
        val position = childList.indexOf(handle)
        val before = childList.take(position).count { it is ResizablePanelWidget } - 1
        return before.takeIf { it >= 0 }
    }

    /**
     * Holds the group metrics and formatting.
     */
    companion object {
        /**
         * The thickness of a handle along the axis.
         */
        const val HANDLE: Int = 1

        /**
         * The share a handle moves by with an arrow key, in percent.
         */
        const val KEY_STEP: Double = 5.0

        /**
         * Rounds a share to one decimal.
         *
         * @param value the share
         * @return the rounded share
         */
        private fun round(value: Double): Double = (value * 10).roundToInt() / 10.0

        /**
         * Formats a share with at most one decimal.
         *
         * @param value the share
         * @return the share as text
         */
        fun format(value: Double): String {
            val rounded = round(value)
            return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
        }
    }
}

/**
 * A panel of a resizable group: its content, stacked across the panel.
 *
 * @param id the id of the widget
 * @property defaultSize the share the panel takes at first, in percent, or `0` to share what the
 *           other panels leave
 * @property minSize the smallest share the panel can take, in percent
 * @property maxSize the largest share the panel can take, in percent
 */
class ResizablePanelWidget(id: String, val defaultSize: Double, val minSize: Double, val maxSize: Double) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }

    /**
     * Draws the content clipped to the panel.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.clipped(bounds) { super.render(ui, context, mouseX, mouseY) }
    }
}

/**
 * A handle between two panels of a resizable group: a thin line in the border colour, optionally
 * with a grip. It is dragged with the mouse, and moved with the arrow keys along the group's axis
 * and to the limits with Home and End while it has the focus.
 *
 * @param id the id of the widget
 * @property withHandle whether a grip is drawn on the line
 */
class ResizableHandleWidget(id: String, val withHandle: Boolean) : Widget(id) {

    /**
     * The group the handle belongs to, set by the factory.
     */
    var group: ResizablePanelGroupWidget? = null

    /**
     * Where on the handle it was grabbed, along the group's axis.
     */
    private var grabOffset: Int = 0

    /**
     * Whether the handle can take the focus: while it is enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * Whether the handle can be dragged: always, while it is enabled.
     */
    override val draggable: Boolean get() = enabled

    /**
     * The handle draws its own focus indication.
     */
    override val drawsOwnFocus: Boolean get() = true

    /**
     * Returns the thickness of the handle.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(ResizablePanelGroupWidget.HANDLE, ResizablePanelGroupWidget.HANDLE)

    /**
     * Returns whether a point is on the handle or within a few pixels of it.
     *
     * @param x the x position
     * @param y the y position
     * @return whether the point grabs the handle
     */
    override fun isOver(x: Double, y: Double): Boolean {
        val b = bounds
        val horizontal = group?.orientation != Orientation.VERTICAL
        return if (horizontal) x >= b.x - GRAB && x < b.right + GRAB && y >= b.y && y < b.bottom
        else y >= b.y - GRAB && y < b.bottom + GRAB && x >= b.x && x < b.right
    }

    /**
     * Draws the line, in the ring colour while focused, and the grip.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val focused = context.focusedWidget === this && context.focusVisible
        ui.fill(bounds, if (focused) ui.tokens.ring else ui.tokens.border)
        if (!withHandle) return
        val horizontal = group?.orientation != Orientation.VERTICAL
        val grip = if (horizontal) Rect(bounds.x - GRIP_THIN / 2, bounds.y + (bounds.height - GRIP_LONG) / 2, GRIP_THIN, GRIP_LONG)
        else Rect(bounds.x + (bounds.width - GRIP_LONG) / 2, bounds.y - GRIP_THIN / 2, GRIP_LONG, GRIP_THIN)
        ui.fillRounded(grip, ui.tokens.border, 1)
        ui.icon(if (horizontal) "grip-vertical" else "grip-horizontal", Rect(grip.x + (grip.width - GRIP_ICON) / 2, grip.y + (grip.height - GRIP_ICON) / 2, GRIP_ICON, GRIP_ICON), ui.tokens.foreground)
    }

    /**
     * Focuses the handle on a left click and remembers where it was grabbed.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on the handle
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            grabOffset = if (group?.orientation == Orientation.VERTICAL) y.toInt() - bounds.y else x.toInt() - bounds.x
        }
        return true
    }

    /**
     * Moves the handle with the mouse and reports the new shares after a pause.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     */
    override fun mouseDragged(context: UiContext, x: Double, y: Double) {
        val group = group ?: return
        val position = (if (group.orientation == Orientation.VERTICAL) y.toInt() else x.toInt()) - grabOffset
        if (group.moveTo(this, position)) changed(context, group, immediate = false)
    }

    /**
     * Moves the handle by a step with the arrow keys along the group's axis, and to the limits
     * with Home and End, reporting the new shares.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key moved the handle
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        val group = group ?: return false
        val vertical = group.orientation == Orientation.VERTICAL
        val delta = when (event.key()) {
            if (vertical) GLFW.GLFW_KEY_DOWN else GLFW.GLFW_KEY_RIGHT -> ResizablePanelGroupWidget.KEY_STEP
            if (vertical) GLFW.GLFW_KEY_UP else GLFW.GLFW_KEY_LEFT -> -ResizablePanelGroupWidget.KEY_STEP
            GLFW.GLFW_KEY_HOME -> -100.0
            GLFW.GLFW_KEY_END -> 100.0
            else -> return false
        }
        if (group.moveBy(this, delta)) changed(context, group, immediate = true)
        return true
    }

    /**
     * Reports new shares of the group and lays the screen out again.
     *
     * @param context the screen showing the widget
     * @param group the group
     * @param immediate whether to report at once
     */
    private fun changed(context: UiContext, group: ResizablePanelGroupWidget, immediate: Boolean) {
        group.markChanged(context, immediate)
        context.requestLayout()
    }

    /**
     * Holds the handle metrics.
     */
    private companion object {
        /**
         * How far beside the line the mouse still grabs the handle.
         */
        const val GRAB: Int = 2

        /**
         * The short side of the grip.
         */
        const val GRIP_THIN: Int = 6

        /**
         * The long side of the grip.
         */
        const val GRIP_LONG: Int = 8

        /**
         * The size of the grip icon.
         */
        const val GRIP_ICON: Int = 6
    }
}

package dev.slne.surf.roleplay.fabric.toast

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.text.TextBlock
import dev.slne.surf.roleplay.protocol.toast.ToastButtonKind
import dev.slne.surf.roleplay.protocol.toast.ToastShow

/**
 * A toast in the stack.
 *
 * @property packet the toast as the server sent it
 * @property remaining how long the toast is still shown, in milliseconds, or `null` if it stays
 *           until dismissed
 */
class ToastEntry(var packet: ToastShow, var remaining: Long?)

/**
 * The toasts shown at the bottom right, oldest first. A toast is replaced in place by a toast with
 * the same id, counts its duration down only while the stack is not paused, and disappears when
 * its time is up, when it is dismissed, or when one of its buttons is clicked.
 */
class ToastStack {

    /**
     * The toasts, oldest first.
     */
    private val entries = mutableListOf<ToastEntry>()

    /**
     * The time of the last update, in milliseconds.
     */
    private var lastUpdate: Long? = null

    /**
     * Shows a toast, or replaces the shown toast with the same id.
     *
     * @param packet the toast
     * @param now the time in milliseconds
     */
    fun show(packet: ToastShow, now: Long) {
        val remaining = packet.durationMillis.takeIf { it > 0 }?.toLong()
        val existing = entries.firstOrNull { it.packet.id == packet.id }
        if (existing != null) {
            existing.packet = packet
            existing.remaining = remaining
        } else {
            entries += ToastEntry(packet, remaining)
        }
        if (lastUpdate == null) lastUpdate = now
    }

    /**
     * Removes a toast.
     *
     * @param id the id of the toast
     */
    fun dismiss(id: String) {
        entries.removeAll { it.packet.id == id }
    }

    /**
     * Removes every toast.
     */
    fun clear() {
        entries.clear()
    }

    /**
     * Counts the durations down to a time and removes the toasts whose time is up.
     *
     * @param now the time in milliseconds
     * @param paused whether the durations stand still, as while the mouse rests on the toasts
     * @return the shown toasts, oldest first
     */
    fun update(now: Long, paused: Boolean): List<ToastEntry> {
        val elapsed = now - (lastUpdate ?: now)
        lastUpdate = now
        if (!paused && elapsed > 0) entries.forEach { entry -> entry.remaining = entry.remaining?.minus(elapsed) }
        entries.removeAll { (it.remaining ?: 1) <= 0 }
        return entries.toList()
    }

    /**
     * Handles a click at a point of the expanded stack: a button click reports the button and
     * removes the toast, a click on the close button removes it, and any other click on a toast is
     * taken without an effect.
     *
     * @param measurer the text measurer
     * @param window the window area
     * @param x the mouse x position
     * @param y the mouse y position
     * @param report sends the click of a button to the server
     * @return whether the click was on a toast
     */
    fun click(measurer: TextMeasurer, window: Rect, x: Double, y: Double, report: (String, ToastButtonKind) -> Unit): Boolean {
        val hit = ToastLayout.place(measurer, entries.toList(), window, expanded = true).lastOrNull { it.area.contains(x, y) } ?: return false
        val id = hit.entry.packet.id
        when {
            hit.action?.contains(x, y) == true -> {
                report(id, ToastButtonKind.ACTION)
                dismiss(id)
            }
            hit.cancel?.contains(x, y) == true -> {
                report(id, ToastButtonKind.CANCEL)
                dismiss(id)
            }
            hit.close?.contains(x, y) == true -> dismiss(id)
        }
        return true
    }
}

/**
 * A toast placed in the window, with the areas of its buttons.
 *
 * @property entry the toast
 * @property area the area of the toast
 * @property depth how far behind the front toast it is drawn in the collapsed stack, `0` for the
 *           front
 * @property action the area of the action button, or `null` for none
 * @property cancel the area of the cancel button, or `null` for none
 * @property close the area of the close button, or `null` for none
 */
data class PlacedToast(val entry: ToastEntry, val area: Rect, val depth: Int, val action: Rect?, val cancel: Rect?, val close: Rect?)

/**
 * Places toasts at the bottom right of the window. Expanded, the newest toasts are stacked upwards
 * with a gap; collapsed, the newest is in front and older ones peek out above it.
 */
object ToastLayout {

    /**
     * The width of a toast.
     */
    const val WIDTH: Int = 180

    /**
     * The space between the toasts and the window edges.
     */
    const val MARGIN: Int = 8

    /**
     * The space inside a toast.
     */
    const val PADDING: Int = 8

    /**
     * The space between two expanded toasts.
     */
    const val GAP: Int = 4

    /**
     * The largest number of toasts shown at once.
     */
    const val VISIBLE: Int = 3

    /**
     * The size of the type icon.
     */
    const val ICON: Int = 8

    /**
     * The height of a button.
     */
    const val BUTTON_HEIGHT: Int = 12

    /**
     * The space left and right of a button caption.
     */
    const val BUTTON_PADDING: Int = 5

    /**
     * The size of the close button.
     */
    const val CLOSE: Int = 6

    /**
     * How far each older toast of the collapsed stack peeks out above the one in front.
     */
    const val PEEK: Int = 4

    /**
     * Returns the width left for the texts of a toast.
     *
     * @param entry the toast
     * @return the width
     */
    fun textWidth(entry: ToastEntry): Int =
        WIDTH - 2 * PADDING - (if (hasIcon(entry)) ICON + PADDING / 2 else 0) - (if (entry.packet.closeButton) CLOSE + PADDING / 2 else 0)

    /**
     * Returns whether a toast has a type icon.
     *
     * @param entry the toast
     * @return whether it has one
     */
    fun hasIcon(entry: ToastEntry): Boolean = entry.packet.type != dev.slne.surf.roleplay.protocol.toast.ToastType.DEFAULT

    /**
     * Computes the height of a toast.
     *
     * @param measurer the text measurer
     * @param entry the toast
     * @return the height
     */
    fun height(measurer: TextMeasurer, entry: ToastEntry): Int {
        val width = textWidth(entry)
        var height = TextBlock.size(measurer, entry.packet.title, width).height
        entry.packet.description?.let { height += 2 + TextBlock.size(measurer, it, width).height }
        if (entry.packet.actionLabel != null || entry.packet.cancelLabel != null) height += GAP + BUTTON_HEIGHT
        return height + 2 * PADDING
    }

    /**
     * Places the newest toasts.
     *
     * @param measurer the text measurer
     * @param entries the toasts, oldest first
     * @param window the window area
     * @param expanded whether the toasts are stacked with a gap instead of peeking out behind the
     *        newest
     * @return the placed toasts, oldest first, so that the newest is drawn last
     */
    fun place(measurer: TextMeasurer, entries: List<ToastEntry>, window: Rect, expanded: Boolean): List<PlacedToast> {
        val shown = entries.takeLast(VISIBLE)
        if (shown.isEmpty()) return emptyList()
        val x = window.right - MARGIN - WIDTH
        val bottom = window.bottom - MARGIN
        val placed = ArrayList<PlacedToast>(shown.size)
        if (expanded) {
            var y = bottom
            for (entry in shown.reversed()) {
                val height = height(measurer, entry)
                y -= height
                placed += placeButtons(measurer, entry, Rect(x, y, WIDTH, height), 0)
                y -= GAP
            }
        } else {
            val front = shown.last()
            val frontHeight = height(measurer, front)
            shown.reversed().forEachIndexed { depth, entry ->
                val inset = depth * PEEK
                val area = Rect(x + inset, bottom - frontHeight - inset, WIDTH - 2 * inset, frontHeight)
                placed += if (depth == 0) placeButtons(measurer, entry, area, 0) else PlacedToast(entry, area, depth, null, null, null)
            }
        }
        return placed.reversed()
    }

    /**
     * Places the buttons of a toast inside its area: the action at the bottom right, the cancel
     * button before it, and the close button at the top right.
     *
     * @param measurer the text measurer
     * @param entry the toast
     * @param area the area of the toast
     * @param depth the depth of the toast in the stack
     * @return the placed toast
     */
    private fun placeButtons(measurer: TextMeasurer, entry: ToastEntry, area: Rect, depth: Int): PlacedToast {
        val y = area.bottom - PADDING - BUTTON_HEIGHT
        var right = area.right - PADDING
        val action = entry.packet.actionLabel?.let {
            val width = measurer.width(it) + 2 * BUTTON_PADDING
            Rect(right - width, y, width, BUTTON_HEIGHT).also { rect -> right = rect.x - GAP }
        }
        val cancel = entry.packet.cancelLabel?.let {
            val width = measurer.width(it) + 2 * BUTTON_PADDING
            Rect(right - width, y, width, BUTTON_HEIGHT)
        }
        val close = if (entry.packet.closeButton && entry.packet.dismissible) Rect(area.right - PADDING - CLOSE, area.y + PADDING, CLOSE, CLOSE) else null
        return PlacedToast(entry, area, depth, action, cancel, close)
    }
}

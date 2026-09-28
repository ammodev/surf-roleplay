package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import dev.slne.surf.roleplay.protocol.screen.Presentation

/**
 * Decides which stacked screens are drawn.
 */
object ScreenLayers {

    /**
     * Returns the positions of the screens that are drawn, from the topmost full screen to the top
     * of the stack. Dialogs and sheets are drawn over the screens below them down to the nearest
     * full screen; screens below that are hidden.
     *
     * @param presentations the presentations of the stacked screens, from bottom to top
     * @return the positions of the drawn screens, or an empty range for an empty stack
     */
    fun visible(presentations: List<Presentation>): IntRange {
        if (presentations.isEmpty()) return IntRange.EMPTY
        val base = presentations.indexOfLast { it == Presentation.SCREEN }.coerceAtLeast(0)
        return base..presentations.lastIndex
    }
}

/**
 * The vertical scroll position of content inside a viewport.
 */
class PanelScroll {

    /**
     * How far the content is scrolled, in GUI pixels from its top.
     */
    var offset: Int = 0
        private set

    /**
     * The largest offset the current content and viewport allow.
     */
    var maxOffset: Int = 0
        private set

    /**
     * The height of the viewport.
     */
    var viewportHeight: Int = 0
        private set

    /**
     * Sets the content and viewport heights and keeps the offset within the new range.
     *
     * @param contentHeight the height of the content
     * @param viewportHeight the height of the viewport
     */
    fun update(contentHeight: Int, viewportHeight: Int) {
        this.viewportHeight = viewportHeight.coerceAtLeast(0)
        maxOffset = (contentHeight - this.viewportHeight).coerceAtLeast(0)
        offset = offset.coerceIn(0, maxOffset)
    }

    /**
     * Scrolls by an amount.
     *
     * @param delta the amount in GUI pixels; positive values scroll towards the top
     * @return whether the offset changed
     */
    fun scrollBy(delta: Int): Boolean {
        val next = (offset - delta).coerceIn(0, maxOffset)
        val moved = next != offset
        offset = next
        return moved
    }

    /**
     * Scrolls as little as needed to make a band of the content visible.
     *
     * @param top the top of the band, in content coordinates
     * @param bottom the bottom of the band, in content coordinates
     */
    fun ensureVisible(top: Int, bottom: Int) {
        if (bottom > offset + viewportHeight) offset = bottom - viewportHeight
        if (top < offset) offset = top
        offset = offset.coerceIn(0, maxOffset)
    }
}

/**
 * Moves the keyboard focus through a widget tree.
 */
object FocusOrder {

    /**
     * Returns the widget that receives the focus after a Tab or Shift+Tab: the next or previous
     * focusable widget in tree order, wrapping at the ends. Without a current widget, or with one
     * that is no longer in the tree, it starts at the first or last focusable widget.
     *
     * @param root the root of the tree
     * @param current the focused widget, or `null`
     * @param backwards whether to move backwards, as for Shift+Tab
     * @return the widget to focus, or `null` if the tree has no focusable widget
     */
    fun next(root: Widget, current: Widget?, backwards: Boolean): Widget? {
        val focusable = mutableListOf<Widget>()
        WidgetTree.visit(root) { if (it.focusable) focusable += it }
        if (focusable.isEmpty()) return null
        val index = focusable.indexOfFirst { it === current }
        if (index < 0) return if (backwards) focusable.last() else focusable.first()
        val step = if (backwards) -1 else 1
        return focusable[(index + step + focusable.size) % focusable.size]
    }
}

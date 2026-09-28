package dev.slne.surf.roleplay.fabric.screen

import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import dev.slne.surf.roleplay.protocol.screen.ScreenStack

/**
 * Rules the mod applies to server-driven screens.
 */
object ScreenRules {

    /**
     * Checks whether a screen open names a parent that is no longer open, in which case the mod
     * drops the open and reports the new session as closed.
     *
     * @param stack the mod's screen stack
     * @param parentSessionId the parent session of the open, or `null`
     * @return whether the open is stale
     */
    fun isStaleOpen(stack: ScreenStack<*>, parentSessionId: Int?): Boolean =
        parentSessionId != null && stack.find(parentSessionId) == null

    /**
     * Checks whether a widget that holds the focus or an open dropdown list can keep it: it must
     * still be the same widget in the tree and be enabled.
     *
     * @param root the root of the tree
     * @param widget the widget
     * @return whether the widget is still usable
     */
    fun isStillUsable(root: Widget, widget: Widget): Boolean =
        widget.enabled && WidgetTree.find(root, widget.id) === widget
}

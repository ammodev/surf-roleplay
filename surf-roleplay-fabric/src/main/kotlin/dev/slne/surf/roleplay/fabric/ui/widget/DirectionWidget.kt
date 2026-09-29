package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.protocol.screen.LayoutDirection
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import dev.slne.surf.roleplay.protocol.screen.SidebarSide

/**
 * Sets the layout direction of its children, stacked. Inside a right-to-left direction, rows
 * start at the right, start and end alignment swap, and components with a side use the mirrored
 * side; text is still drawn left to right.
 *
 * @param id the id of the widget
 * @property direction the direction of the children
 */
class DirectionWidget(id: String, val direction: LayoutDirection) : ContainerWidget(id, Axis.VERTICAL)

/**
 * Returns the side a component with this side uses in a direction: left and right swap when
 * right-to-left, top and bottom stay.
 *
 * @param rtl whether the component is right-to-left
 * @return the side to use
 */
fun OverlaySide.inDirection(rtl: Boolean): OverlaySide = when {
    !rtl -> this
    this == OverlaySide.LEFT -> OverlaySide.RIGHT
    this == OverlaySide.RIGHT -> OverlaySide.LEFT
    else -> this
}

/**
 * Returns the side a sidebar with this side uses in a direction: left and right swap when
 * right-to-left.
 *
 * @param rtl whether the sidebar is right-to-left
 * @return the side to use
 */
fun SidebarSide.inDirection(rtl: Boolean): SidebarSide = when {
    !rtl -> this
    this == SidebarSide.LEFT -> SidebarSide.RIGHT
    else -> SidebarSide.LEFT
}

/**
 * Returns the Lucide icon that points the other way when right-to-left: every `left` part of
 * the name becomes `right` and every `right` part becomes `left`, as in `chevron-right`. Names
 * without such a part stay.
 *
 * @param name the Lucide name of the icon
 * @param rtl whether the icon is drawn right-to-left
 * @return the name to draw
 */
fun directionalIcon(name: String, rtl: Boolean): String {
    if (!rtl) return name
    return name.split('-').joinToString("-") { part ->
        when (part) {
            "left" -> "right"
            "right" -> "left"
            else -> part
        }
    }
}

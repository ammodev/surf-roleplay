package dev.slne.surf.roleplay.fabric.interaction

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.hypot

/**
 * An item on a ring of the radial menu.
 */
sealed interface RingItem {
    /**
     * A node of the server's tree.
     *
     * @property index the node index
     */
    data class Node(val index: Int) : RingItem

    /**
     * The "Mehr…" item that opens the entries that did not fit.
     *
     * @property rest the node indices that did not fit
     */
    data class More(val rest: List<Int>) : RingItem
}

/**
 * The geometry of one ring of the radial menu.
 */
object RadialLayout {

    /**
     * The largest number of items on a ring.
     */
    const val MAX_PER_RING: Int = 8

    /**
     * Arranges the children of a level on a ring: all of them if they fit, otherwise the first
     * seven and a "Mehr…" item with the rest.
     *
     * @param children the node indices of the level in order
     * @return the ring items
     */
    fun ring(children: List<Int>): List<RingItem> =
        if (children.size <= MAX_PER_RING) {
            children.map { RingItem.Node(it) }
        } else {
            children.take(MAX_PER_RING - 1).map { RingItem.Node(it) } + RingItem.More(children.drop(MAX_PER_RING - 1))
        }

    /**
     * Returns the angle of a slot's centre in radians, with slot 0 at the top and slots running
     * clockwise.
     *
     * @param slot the slot
     * @param count the number of slots
     * @return the angle, 0 pointing right and growing clockwise on screen
     */
    fun angle(slot: Int, count: Int): Double = -PI / 2 + 2 * PI * slot / count

    /**
     * Returns the slot under a direction from the centre.
     *
     * @param dx the horizontal offset from the centre
     * @param dy the vertical offset from the centre, positive downwards
     * @param count the number of slots
     * @param innerRadius the radius of the centre area that selects no slot
     * @return the slot, or `null` inside the centre area or for an empty ring
     */
    fun hovered(dx: Double, dy: Double, count: Int, innerRadius: Double): Int? {
        if (count == 0 || hypot(dx, dy) < innerRadius) return null
        val fromTop = (atan2(dy, dx) + PI / 2 + 2 * PI) % (2 * PI)
        val width = 2 * PI / count
        return floor((fromTop + width / 2) / width).toInt() % count
    }
}

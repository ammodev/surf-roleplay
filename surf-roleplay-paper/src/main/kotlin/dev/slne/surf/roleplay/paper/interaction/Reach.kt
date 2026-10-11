package dev.slne.surf.roleplay.paper.interaction

import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Geometry of the interaction reach.
 */
object Reach {
    /**
     * Returns the distance from a point to the nearest point of a box, `0` inside the box.
     *
     * @param eye the point, usually the player's eye position
     * @param box the box, usually the target's bounding box
     * @return the distance in blocks
     */
    fun distanceToBox(eye: Vector, box: BoundingBox): Double {
        val dx = max(max(box.minX - eye.x, 0.0), eye.x - box.maxX)
        val dy = max(max(box.minY - eye.y, 0.0), eye.y - box.maxY)
        val dz = max(max(box.minZ - eye.z, 0.0), eye.z - box.maxZ)
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

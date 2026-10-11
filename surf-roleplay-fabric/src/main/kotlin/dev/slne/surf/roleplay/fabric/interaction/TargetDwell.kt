package dev.slne.surf.roleplay.fabric.interaction

/**
 * A target as the protocol names it.
 *
 * @property kind the target kind
 * @property id the target id
 */
data class TargetRef(val kind: String, val id: String)

/**
 * Decides when to query a target: once it has rested under the crosshair for the dwell, and never
 * while paused because the cursor is over the option list.
 *
 * @property dwellMillis how long a target must rest before it is queried
 */
class TargetDwell(private val dwellMillis: Long = 120) {

    /** The target that is resting. */
    private var target: TargetRef? = null

    /** When [target] started resting. */
    private var since: Long = 0

    /** Whether [target] was queried. */
    private var sent: Boolean = false

    /**
     * Applies the target of the current frame.
     *
     * @param now the time in milliseconds
     * @param current the target under the crosshair, or `null` for none
     * @param paused whether queries are paused
     * @return the target to query now, or `null`
     */
    fun update(now: Long, current: TargetRef?, paused: Boolean): TargetRef? {
        if (paused) return null
        if (current != target) {
            target = current
            since = now
            sent = false
            return null
        }
        if (current == null || sent || now - since < dwellMillis) return null
        sent = true
        return current
    }

    /**
     * Forgets the resting target so that it is queried again after the dwell.
     */
    fun reset() {
        target = null
        sent = false
    }
}

package dev.slne.surf.roleplay.paper.interaction

import java.util.Locale

/**
 * Decides whether a target is reachable from its distance and line of sight.
 */
object TargetCheck {
    /**
     * The decision.
     */
    sealed interface Result {
        /**
         * The target is reachable.
         */
        data object Passed : Result

        /**
         * The target is not reachable.
         *
         * @property reason why, for the log
         */
        data class Failed(val reason: String) : Result
    }

    /**
     * Checks a target.
     *
     * @param distance the distance from the eyes to the target surface
     * @param range the allowed range
     * @param inSight whether no block stands between the eyes and the target
     * @return the decision
     */
    fun check(distance: Double, range: Double, inSight: Boolean): Result = when {
        distance.isNaN() || range.isNaN() -> Result.Failed("invalid distance or range")
        distance > range -> Result.Failed("out of range (%.2f > %.2f)".format(Locale.ROOT, distance, range))
        !inSight -> Result.Failed("not in line of sight")
        else -> Result.Passed
    }
}

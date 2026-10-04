package dev.slne.surf.roleplay.paper.tablist

import dev.slne.surf.roleplay.protocol.tablist.OnlineLevel

/**
 * Maps online counts to the coarse levels shown in the tab list.
 */
object OnlineLevels {

    /**
     * The default thresholds: one member is a few, three or more are many.
     */
    val DEFAULT_THRESHOLDS: List<Int> = listOf(1, 3)

    /**
     * Returns the level of a count: below the first threshold is [OnlineLevel.NONE], below the
     * second is [OnlineLevel.FEW], otherwise [OnlineLevel.MANY].
     *
     * @param count the number of members online
     * @param thresholds exactly two strictly increasing, non-negative values
     * @return the level of the count
     * @throws IllegalArgumentException if the thresholds are not two strictly increasing,
     *         non-negative values
     */
    fun level(count: Int, thresholds: List<Int> = DEFAULT_THRESHOLDS): OnlineLevel {
        require(thresholds.size == 2) { "Exactly two thresholds are required" }
        require(thresholds[0] >= 0 && thresholds[0] < thresholds[1]) {
            "Thresholds must be non-negative and strictly increasing"
        }
        return when {
            count < thresholds[0] -> OnlineLevel.NONE
            count < thresholds[1] -> OnlineLevel.FEW
            else -> OnlineLevel.MANY
        }
    }
}

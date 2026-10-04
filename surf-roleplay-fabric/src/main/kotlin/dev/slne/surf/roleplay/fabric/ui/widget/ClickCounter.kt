package dev.slne.surf.roleplay.fabric.ui.widget

import kotlin.math.hypot

/**
 * Counts consecutive clicks to tell single, double and triple clicks apart.
 *
 * A click continues the count when it comes within [INTERVAL_MILLIS] of the previous click and
 * within [MAX_DISTANCE] pixels of it. The count goes from one to three; the click after a triple
 * click counts as a single click again.
 */
class ClickCounter {

    /**
     * The number of the previous click in its run, `0` before the first click.
     */
    private var count: Int = 0

    /**
     * The time of the previous click in milliseconds.
     */
    private var lastTime: Long = 0

    /**
     * The x position of the previous click.
     */
    private var lastX: Double = 0.0

    /**
     * The y position of the previous click.
     */
    private var lastY: Double = 0.0

    /**
     * Records a click and returns its number in the current run.
     *
     * @param x the mouse x position
     * @param y the mouse y position
     * @param timeMillis the time of the click in milliseconds
     * @return `1` for a single click, `2` for a double click, `3` for a triple click
     */
    fun click(x: Double, y: Double, timeMillis: Long): Int {
        val continues = count in 1 until MAX_COUNT &&
            timeMillis - lastTime in 0..INTERVAL_MILLIS &&
            hypot(x - lastX, y - lastY) <= MAX_DISTANCE
        count = if (continues) count + 1 else 1
        lastTime = timeMillis
        lastX = x
        lastY = y
        return count
    }

    /**
     * Forgets the previous clicks, so that the next click counts as a single click.
     */
    fun reset() {
        count = 0
    }

    /**
     * Holds the limits of a run of clicks.
     */
    companion object {
        /**
         * The longest time between two clicks of one run, in milliseconds.
         */
        const val INTERVAL_MILLIS: Long = 500

        /**
         * The largest distance between two clicks of one run, in pixels.
         */
        const val MAX_DISTANCE: Double = 4.0

        /**
         * The highest count of a run.
         */
        const val MAX_COUNT: Int = 3
    }
}

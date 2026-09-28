package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeTokens
import dev.slne.surf.roleplay.protocol.screen.IconColor
import kotlin.math.PI
import kotlin.math.cos

/**
 * Returns the colour of an icon colour token in a theme.
 *
 * @param tokens the theme tokens
 * @param color the colour token
 * @return the ARGB colour
 */
internal fun iconTint(tokens: ThemeTokens, color: IconColor): Int = when (color) {
    IconColor.FOREGROUND -> tokens.foreground
    IconColor.MUTED -> tokens.mutedForeground
    IconColor.PRIMARY -> tokens.primary
    IconColor.DESTRUCTIVE -> tokens.destructive
}

/**
 * A placeholder in the accent colour that pulses while content is loading. It takes the size its
 * node gives it.
 *
 * @param id the id of the widget
 * @property round whether the placeholder is a circle or pill instead of a rounded rectangle
 */
class SkeletonWidget(id: String, val round: Boolean) : Widget(id) {

    /**
     * Returns nothing: a skeleton has no content of its own.
     *
     * @param measurer the text measurer
     * @return the empty size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size.ZERO

    /**
     * Draws the placeholder at the current pulse opacity.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val color = ThemeColors.withAlpha(ui.tokens.accent, opacity(System.currentTimeMillis()))
        ui.fillRounded(bounds, color, if (round) minOf(bounds.width, bounds.height) / 2 else RADIUS)
    }

    /**
     * Holds the pulse.
     */
    companion object {
        /**
         * The length of one pulse in milliseconds.
         */
        private const val PERIOD_MILLIS: Long = 2000

        /**
         * The corner radius of a rectangular placeholder.
         */
        private const val RADIUS: Int = 4

        /**
         * Returns the opacity of a pulsing placeholder at a time: full at the start of every pulse
         * and half in its middle.
         *
         * @param timeMillis the time in milliseconds
         * @return the opacity, from `0.5` to `1`
         */
        fun opacity(timeMillis: Long): Float {
            val phase = (timeMillis % PERIOD_MILLIS).toDouble() / PERIOD_MILLIS
            return (0.75 + 0.25 * cos(2 * PI * phase)).toFloat()
        }
    }
}

/**
 * A loading indicator: the Lucide loader circle, turning once a second.
 *
 * @param id the id of the widget
 * @property size the side length of the indicator when it fits its content
 * @property color the theme colour the indicator is drawn in
 */
class SpinnerWidget(id: String, val size: Int, val color: IconColor) : Widget(id) {

    /**
     * Returns a square of the indicator size.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(size, size)

    /**
     * Draws the indicator square and centered, turned to the current angle.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val side = minOf(bounds.width, bounds.height)
        val rect = Rect(bounds.x + (bounds.width - side) / 2, bounds.y + (bounds.height - side) / 2, side, side)
        ui.rotatedIcon(ICON, rect, iconTint(ui.tokens, color), angle(System.currentTimeMillis()))
    }

    /**
     * Holds the rotation.
     */
    companion object {
        /**
         * The Lucide name of the indicator icon.
         */
        const val ICON: String = "loader-circle"

        /**
         * The length of one turn in milliseconds.
         */
        private const val TURN_MILLIS: Long = 1000

        /**
         * Returns the angle of the indicator at a time.
         *
         * @param timeMillis the time in milliseconds
         * @return the angle in degrees clockwise, from `0` to below `360`
         */
        fun angle(timeMillis: Long): Float = (timeMillis % TURN_MILLIS) * 360f / TURN_MILLIS
    }
}

/**
 * A box whose height follows its width in a fixed ratio. Its content is laid out over the whole
 * box.
 *
 * @param id the id of the widget
 * @property ratio the width divided by the height
 */
class AspectRatioWidget(id: String, val ratio: Float) : ContainerWidget(id, Axis.VERTICAL) {

    /**
     * Creates the layout box of the ratio box with the boxes of its content.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox = LayoutBox(
        width = width,
        axis = Axis.VERTICAL,
        children = childList.map { it.createLayout(measurer) },
        aspectRatio = ratio.coerceAtLeast(MIN_RATIO),
    ).also { layoutBox = it }

    /**
     * Holds the smallest ratio.
     */
    private companion object {
        /**
         * The smallest ratio, which keeps the height finite.
         */
        const val MIN_RATIO: Float = 0.01f
    }
}

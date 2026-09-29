package dev.slne.surf.roleplay.fabric.ui.theme

import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import kotlin.math.roundToInt

/**
 * The design tokens of one theme variant, as ARGB colours.
 *
 * @property background the colour behind everything
 * @property foreground the colour of regular text
 * @property card the colour of panels
 * @property cardForeground the colour of text on panels
 * @property popover the colour of floating surfaces such as option lists
 * @property popoverForeground the colour of text on floating surfaces
 * @property primary the colour of primary actions
 * @property primaryForeground the colour of text on primary actions
 * @property secondary the colour of secondary actions
 * @property secondaryForeground the colour of text on secondary actions
 * @property muted the colour of subdued surfaces
 * @property mutedForeground the colour of secondary text
 * @property accent the colour of highlighted items, such as a hovered option
 * @property accentForeground the colour of text on highlighted items
 * @property destructive the colour of destructive actions and errors
 * @property destructiveForeground the colour of text on destructive actions
 * @property border the colour of borders
 * @property input the colour of input borders and backgrounds
 * @property ring the colour of the keyboard focus ring
 * @property radius the corner radius of panels and widgets, in GUI pixels
 * @property chart1 the colour of the first chart series
 * @property chart2 the colour of the second chart series
 * @property chart3 the colour of the third chart series
 * @property chart4 the colour of the fourth chart series
 * @property chart5 the colour of the fifth chart series
 */
data class ThemeTokens(
    val background: Int,
    val foreground: Int,
    val card: Int,
    val cardForeground: Int,
    val popover: Int,
    val popoverForeground: Int,
    val primary: Int,
    val primaryForeground: Int,
    val secondary: Int,
    val secondaryForeground: Int,
    val muted: Int,
    val mutedForeground: Int,
    val accent: Int,
    val accentForeground: Int,
    val destructive: Int,
    val destructiveForeground: Int,
    val border: Int,
    val input: Int,
    val ring: Int,
    val radius: Int,
    val chart1: Int,
    val chart2: Int,
    val chart3: Int,
    val chart4: Int,
    val chart5: Int,
) {
    /**
     * Returns the chart colour with a number from 1 to 5; other numbers wrap around, so 6 is the
     * first colour again and 0 or less is the first.
     *
     * @param number the number of the chart colour
     * @return the colour
     */
    fun chart(number: Int): Int = when ((number - 1).coerceAtLeast(0) % 5) {
        0 -> chart1
        1 -> chart2
        2 -> chart3
        3 -> chart4
        else -> chart5
    }
}

/**
 * The themes the mod ships, each in a dark and a light variant.
 */
object Themes {

    /**
     * The name of the theme used when a screen names an unknown theme.
     */
    const val DEFAULT: String = "default"

    /**
     * The neutral default theme, dark variant.
     */
    private val defaultDark = ThemeTokens(
        background = opaque(0x0A0A0A), foreground = opaque(0xFAFAFA),
        card = opaque(0x171717), cardForeground = opaque(0xFAFAFA),
        popover = opaque(0x171717), popoverForeground = opaque(0xFAFAFA),
        primary = opaque(0xE5E5E5), primaryForeground = opaque(0x171717),
        secondary = opaque(0x262626), secondaryForeground = opaque(0xFAFAFA),
        muted = opaque(0x262626), mutedForeground = opaque(0xA1A1A1),
        accent = opaque(0x262626), accentForeground = opaque(0xFAFAFA),
        destructive = opaque(0xFF6467), destructiveForeground = opaque(0xFAFAFA),
        border = translucentWhite(0.10f), input = translucentWhite(0.15f),
        ring = opaque(0x737373), radius = 3,
        chart1 = opaque(0x2662D9), chart2 = opaque(0x2EB88A), chart3 = opaque(0xE88C30),
        chart4 = opaque(0xAF57DB), chart5 = opaque(0xE23670),
    )

    /**
     * The neutral default theme, light variant.
     */
    private val defaultLight = ThemeTokens(
        background = opaque(0xFFFFFF), foreground = opaque(0x0A0A0A),
        card = opaque(0xFFFFFF), cardForeground = opaque(0x0A0A0A),
        popover = opaque(0xFFFFFF), popoverForeground = opaque(0x0A0A0A),
        primary = opaque(0x171717), primaryForeground = opaque(0xFAFAFA),
        secondary = opaque(0xF5F5F5), secondaryForeground = opaque(0x171717),
        muted = opaque(0xF5F5F5), mutedForeground = opaque(0x737373),
        accent = opaque(0xF5F5F5), accentForeground = opaque(0x171717),
        destructive = opaque(0xE7000B), destructiveForeground = opaque(0xFAFAFA),
        border = opaque(0xE5E5E5), input = opaque(0xE5E5E5),
        ring = opaque(0xA1A1A1), radius = 3,
        chart1 = opaque(0xE76E50), chart2 = opaque(0x2A9D90), chart3 = opaque(0x274754),
        chart4 = opaque(0xE8C468), chart5 = opaque(0xF4A462),
    )

    /**
     * The palettes keyed by theme name and variant.
     */
    private val palettes: Map<String, Map<ThemeVariant, ThemeTokens>> = mapOf(
        DEFAULT to mapOf(ThemeVariant.DARK to defaultDark, ThemeVariant.LIGHT to defaultLight),
        "police" to mapOf(
            ThemeVariant.DARK to defaultDark.copy(primary = opaque(0x3B82F6), primaryForeground = opaque(0xFFFFFF), ring = opaque(0x1D4ED8)),
            ThemeVariant.LIGHT to defaultLight.copy(primary = opaque(0x1D4ED8), primaryForeground = opaque(0xFFFFFF), ring = opaque(0x60A5FA)),
        ),
        "sar" to mapOf(
            ThemeVariant.DARK to defaultDark.copy(primary = opaque(0xF97316), primaryForeground = opaque(0x1C0A00), ring = opaque(0xC2410C)),
            ThemeVariant.LIGHT to defaultLight.copy(primary = opaque(0xEA580C), primaryForeground = opaque(0xFFFFFF), ring = opaque(0xFB923C)),
        ),
    )

    /**
     * Every palette the mod ships.
     */
    val all: List<ThemeTokens> get() = palettes.values.flatMap { it.values }

    /**
     * Looks up the tokens of a theme variant.
     *
     * @param name the theme name
     * @param variant the variant
     * @return the tokens, or the default theme's tokens in the same variant if the name is unknown
     */
    fun resolve(name: String, variant: ThemeVariant): ThemeTokens =
        (palettes[name] ?: palettes.getValue(DEFAULT)).getValue(variant)

    /**
     * Turns an RGB value into an opaque ARGB colour.
     *
     * @param rgb the colour as `0xRRGGBB`
     * @return the ARGB colour
     */
    private fun opaque(rgb: Int): Int = rgb or (0xFF shl 24)

    /**
     * Creates a translucent white.
     *
     * @param alpha the opacity, from `0` to `1`
     * @return the ARGB colour
     */
    private fun translucentWhite(alpha: Float): Int = ThemeColors.withAlpha(opaque(0xFFFFFF), alpha)
}

/**
 * Operations on ARGB colours.
 */
object ThemeColors {

    /**
     * Replaces the opacity of a colour.
     *
     * @param color the ARGB colour
     * @param alpha the new opacity, from `0` to `1`
     * @return the colour with the new opacity
     */
    fun withAlpha(color: Int, alpha: Float): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0f, 1f) * 255).roundToInt() shl 24)

    /**
     * Mixes two colours channel by channel, including opacity.
     *
     * @param from the colour at `0`
     * @param to the colour at `1`
     * @param amount how far to move from [from] towards [to], from `0` to `1`
     * @return the mixed colour
     */
    fun blend(from: Int, to: Int, amount: Float): Int {
        val t = amount.coerceIn(0f, 1f)
        var result = 0
        for (shift in intArrayOf(24, 16, 8, 0)) {
            val a = (from ushr shift) and 0xFF
            val b = (to ushr shift) and 0xFF
            result = result or ((a + (b - a) * t).roundToInt() shl shift)
        }
        return result
    }

    /**
     * Checks whether a colour is dark.
     *
     * @param color the ARGB colour
     * @return whether its relative luminance is below one half
     */
    fun isDark(color: Int): Boolean {
        val r = (color ushr 16) and 0xFF
        val g = (color ushr 8) and 0xFF
        val b = color and 0xFF
        return 0.2126 * r + 0.7152 * g + 0.0722 * b < 128
    }
}

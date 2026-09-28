package dev.slne.surf.roleplay.api.client.common.screen

/**
 * The names of the themes the client mod ships.
 */
object ScreenThemes {
    /**
     * The neutral default theme.
     */
    const val DEFAULT: String = "default"

    /**
     * The theme of the search and rescue organisation.
     */
    const val SAR: String = "sar"

    /**
     * The theme of the police.
     */
    const val POLICE: String = "police"
}

/**
 * The light or dark variant of a screen's theme.
 */
enum class ScreenVariant {
    /**
     * Light text on dark surfaces.
     */
    DARK,

    /**
     * Dark text on light surfaces.
     */
    LIGHT,
}

/**
 * How a screen is shown relative to the screens below it.
 */
enum class ScreenPresentation {
    /**
     * The screen replaces what is shown; the screens below it are hidden.
     */
    SCREEN,

    /**
     * A centered panel over the dimmed screens below it.
     */
    DIALOG,

    /**
     * A panel along one edge of the window over the dimmed screens below it.
     */
    SHEET,
}

/**
 * The window edge a sheet is attached to.
 */
enum class SheetSide {
    /**
     * The right edge.
     */
    RIGHT,

    /**
     * The left edge.
     */
    LEFT,

    /**
     * The top edge.
     */
    TOP,

    /**
     * The bottom edge.
     */
    BOTTOM,
}

/**
 * The theme colour an icon is tinted with.
 */
enum class IconTint {
    /**
     * The regular text colour.
     */
    FOREGROUND,

    /**
     * The secondary text colour.
     */
    MUTED,

    /**
     * The theme's primary colour.
     */
    PRIMARY,

    /**
     * The colour of destructive actions.
     */
    DESTRUCTIVE,
}

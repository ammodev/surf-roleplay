package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The light or dark variant of a screen's theme.
 */
@Serializable
enum class ThemeVariant {
    /**
     * Light text on dark surfaces.
     */
    @ProtoNumber(0)
    DARK,

    /**
     * Dark text on light surfaces.
     */
    @ProtoNumber(1)
    LIGHT,
}

/**
 * How a screen is shown relative to the screens below it.
 */
@Serializable
enum class Presentation {
    /**
     * The screen replaces what is shown; the screens below it are hidden.
     */
    @ProtoNumber(0)
    SCREEN,

    /**
     * A centered panel over the dimmed screens below it.
     */
    @ProtoNumber(1)
    DIALOG,

    /**
     * A panel along one edge of the window over the dimmed screens below it.
     */
    @ProtoNumber(2)
    SHEET,
}

/**
 * The window edge a sheet is attached to.
 */
@Serializable
enum class SheetEdge {
    /**
     * The right edge.
     */
    @ProtoNumber(0)
    RIGHT,

    /**
     * The left edge.
     */
    @ProtoNumber(1)
    LEFT,

    /**
     * The top edge.
     */
    @ProtoNumber(2)
    TOP,

    /**
     * The bottom edge.
     */
    @ProtoNumber(3)
    BOTTOM,
}

/**
 * The theme token an icon is tinted with.
 */
@Serializable
enum class IconColor {
    /**
     * The regular text colour.
     */
    @ProtoNumber(0)
    FOREGROUND,

    /**
     * The secondary text colour.
     */
    @ProtoNumber(1)
    MUTED,

    /**
     * The theme's primary colour.
     */
    @ProtoNumber(2)
    PRIMARY,

    /**
     * The colour of destructive actions.
     */
    @ProtoNumber(3)
    DESTRUCTIVE,
}

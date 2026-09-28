package dev.slne.surf.roleplay.fabric.ui.theme

/**
 * The look of every roleplay screen: colors as ARGB values and sizes in GUI pixels.
 */
object RoleplayTheme {
    /**
     * The background of a screen panel.
     */
    const val PANEL: Int = 0xF0141A22.toInt()

    /**
     * The border of a screen panel.
     */
    const val PANEL_BORDER: Int = 0xFF2E3A48.toInt()

    /**
     * The background of the title bar of a screen panel.
     */
    const val TITLE_BAR: Int = 0xFF1B232D.toInt()

    /**
     * The accent color, used for focus, checked boxes and progress.
     */
    const val ACCENT: Int = 0xFF3FA7D6.toInt()

    /**
     * The color of regular text.
     */
    const val TEXT: Int = 0xFFE8EDF2.toInt()

    /**
     * The color of secondary text, such as placeholders.
     */
    const val TEXT_MUTED: Int = 0xFF8A96A3.toInt()

    /**
     * The color of text on disabled widgets.
     */
    const val TEXT_DISABLED: Int = 0xFF55606B.toInt()

    /**
     * The background of buttons, checkboxes and dropdowns.
     */
    const val WIDGET: Int = 0xFF1E2630.toInt()

    /**
     * The background of buttons, checkboxes and dropdowns under the mouse.
     */
    const val WIDGET_HOVER: Int = 0xFF28323E.toInt()

    /**
     * The background of disabled widgets.
     */
    const val WIDGET_DISABLED: Int = 0xFF161C23.toInt()

    /**
     * The border of widgets.
     */
    const val WIDGET_BORDER: Int = 0xFF36434F.toInt()

    /**
     * The background of text and number inputs.
     */
    const val INPUT: Int = 0xFF0E1318.toInt()

    /**
     * The border of an input whose value breaks its constraints.
     */
    const val INVALID: Int = 0xFFD64545.toInt()

    /**
     * The background of a scroll list's scroll bar track.
     */
    const val SCROLL_TRACK: Int = 0xFF10151B.toInt()

    /**
     * The color of a scroll list's scroll bar handle.
     */
    const val SCROLL_HANDLE: Int = 0xFF3A4754.toInt()

    /**
     * The height of buttons, inputs and dropdowns.
     */
    const val WIDGET_HEIGHT: Int = 20

    /**
     * The default width of text inputs, number inputs and dropdowns that fit their content.
     */
    const val INPUT_WIDTH: Int = 120

    /**
     * The horizontal space inside buttons, inputs and dropdowns.
     */
    const val WIDGET_PADDING: Int = 6

    /**
     * The side length of a checkbox's box.
     */
    const val CHECKBOX_SIZE: Int = 12

    /**
     * The height of a progress bar that fits its content.
     */
    const val PROGRESS_HEIGHT: Int = 12

    /**
     * The width of a progress bar that fits its content.
     */
    const val PROGRESS_WIDTH: Int = 100

    /**
     * The side length of an image that fits its content.
     */
    const val IMAGE_SIZE: Int = 16

    /**
     * The width of a scroll list's scroll bar.
     */
    const val SCROLL_BAR_WIDTH: Int = 4

    /**
     * The GUI pixels a scroll list moves per scroll step.
     */
    const val SCROLL_STEP: Int = 12

    /**
     * The space between a screen panel's edge and its content.
     */
    const val PANEL_PADDING: Int = 8

    /**
     * The height of a screen panel's title bar.
     */
    const val TITLE_BAR_HEIGHT: Int = 16

    /**
     * The smallest space between a screen panel and the window edge.
     */
    const val SCREEN_MARGIN: Int = 8
}

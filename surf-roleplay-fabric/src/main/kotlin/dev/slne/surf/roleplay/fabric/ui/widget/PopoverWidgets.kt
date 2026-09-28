package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.OverlaySide

/**
 * Draws the surface of floating content: a shadow, the popover colour and a border.
 */
object OverlaySurface {

    /**
     * The colour of the shadow below floating content.
     */
    private const val SHADOW: Int = -0x1000000

    /**
     * The opacity of the shadow.
     */
    private const val SHADOW_ALPHA: Float = 0.25f

    /**
     * Draws a surface over an area.
     *
     * @param ui the graphics to draw with
     * @param area the area
     */
    fun draw(ui: UiGraphics, area: Rect) {
        val tokens = ui.tokens
        ui.fillRounded(Rect(area.x, area.y + 1, area.width, area.height + 1), ThemeColors.withAlpha(SHADOW, SHADOW_ALPHA))
        ui.fillRounded(area, tokens.popover)
        ui.borderRounded(area, tokens.border)
    }
}

/**
 * The content of a popover or hover card: a padded stack on a bordered surface.
 *
 * @param id the id of the widget
 */
class PopoverContentWidget(id: String) : OverlayContentWidget(id) {
    init {
        padding = Insets(PADDING, PADDING, PADDING, PADDING)
        gap = GAP
    }

    /**
     * Draws the surface, then the content.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        OverlaySurface.draw(ui, bounds)
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Holds the content spacing.
     */
    companion object {
        /**
         * The space inside the surface.
         */
        const val PADDING: Int = 8

        /**
         * The space between two parts of the content.
         */
        const val GAP: Int = 8
    }
}

/**
 * The header of a popover: its title and description, stacked.
 *
 * @param id the id of the widget
 */
class PopoverHeaderWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = 2
        crossAlign = Align.STRETCH
    }
}

/**
 * A popover: its triggers open and close its content, which is placed on a side of them.
 *
 * @param id the id of the widget
 * @property side the side of the triggers the content opens on
 * @property align how the content is aligned along that side
 */
class PopoverWidget(id: String, val side: OverlaySide, val align: Align) : OverlayHostWidget(id) {

    /**
     * Creates the overlay that shows the content next to the triggers.
     *
     * @return the overlay
     */
    override fun createPopover(): Popover = WidgetPopover(this, side, align)
}

/**
 * Decides when a hover card opens and closes: it opens once the mouse rested on it for a delay,
 * and closes once the mouse was away for another delay.
 *
 * @property openDelay how long the mouse must rest before it opens, in milliseconds
 * @property closeDelay how long the mouse must be away before it closes, in milliseconds
 */
class HoverTimer(val openDelay: Int, val closeDelay: Int) {

    /**
     * When the current hover started, or `null` while the mouse is away.
     */
    private var hoverStart: Long? = null

    /**
     * When the mouse was last seen, or `null` if it never was.
     */
    private var lastSeen: Long? = null

    /**
     * Whether the card is open.
     */
    private var open: Boolean = false

    /**
     * Records whether the mouse is on the card or its triggers at a time and returns whether the
     * card should be open.
     *
     * @param now the time in milliseconds
     * @param hovered whether the mouse is on the triggers or the open content
     * @return whether the card should be open
     */
    fun update(now: Long, hovered: Boolean): Boolean {
        if (hovered) {
            if (hoverStart == null) hoverStart = now
            lastSeen = now
            if (!open && now - (hoverStart ?: now) >= openDelay) open = true
        } else {
            hoverStart = null
            if (open && now - (lastSeen ?: now) >= closeDelay) open = false
        }
        return open
    }

    /**
     * Forgets the current hover, as when the card was closed by other means.
     */
    fun reset() {
        hoverStart = null
        open = false
    }
}

/**
 * A hover card: its content opens next to its triggers while the mouse rests on them, and stays
 * open while the mouse is on the content.
 *
 * @param id the id of the widget
 * @property side the side of the triggers the content opens on
 * @property align how the content is aligned along that side
 * @param openDelay how long the mouse must rest before the content opens, in milliseconds
 * @param closeDelay how long the mouse must be away before the content closes, in milliseconds
 */
class HoverCardWidget(id: String, val side: OverlaySide, val align: Align, openDelay: Int, closeDelay: Int) : OverlayHostWidget(id) {

    /**
     * Actions inside the triggers stay actions; the overlay opens on hover.
     */
    override val togglesOnTriggerAction: Boolean get() = false

    /**
     * The timing of opening and closing.
     */
    val timer: HoverTimer = HoverTimer(openDelay, closeDelay)

    /**
     * When the mouse was last on the open content, in milliseconds.
     */
    internal var contentSeen: Long = 0

    /**
     * Creates the overlay that shows the content and records when the mouse is on it.
     *
     * @return the overlay
     */
    override fun createPopover(): Popover = object : WidgetPopover(this, side, align) {
        /**
         * Draws the content and records whether the mouse is on it.
         *
         * @param ui the graphics to draw with
         * @param context the screen showing the overlay
         * @param area the overlay's area
         * @param mouseX the mouse x position
         * @param mouseY the mouse y position
         */
        override fun render(ui: UiGraphics, context: UiContext, area: Rect, mouseX: Int, mouseY: Int) {
            if (area.contains(mouseX.toDouble(), mouseY.toDouble())) contentSeen = System.currentTimeMillis()
            super.render(ui, context, area, mouseX, mouseY)
        }
    }

    /**
     * Draws the triggers and opens or closes the content as the mouse rests on or leaves the
     * triggers and the content.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        super.render(ui, context, mouseX, mouseY)
        val now = System.currentTimeMillis()
        val hovered = isOver(mouseX, mouseY) || (open && now - contentSeen < FRAME_TOLERANCE_MILLIS)
        val wanted = timer.update(now, hovered)
        if (wanted && !open) show(context, report = true)
        if (!wanted && open) close(context)
    }

    /**
     * Holds the hover tolerance.
     */
    private companion object {
        /**
         * How long the mouse counts as on the content after it was last seen there, which covers
         * the time between two frames.
         */
        const val FRAME_TOLERANCE_MILLIS: Long = 100
    }
}

/**
 * A tooltip around triggers: shows its text on a side of them while one is hovered or focused.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property side the side of the triggers the text is shown on
 */
class TooltipWidget(id: String, var text: String, val side: OverlaySide) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        crossAlign = Align.CENTER
    }

    /**
     * Checks whether the tooltip should show: while the mouse is on the triggers or the focus is
     * inside them.
     *
     * @param focused the focused widget, or `null`
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @return whether to show the text
     */
    fun wantsTooltip(focused: Widget?, mouseX: Int, mouseY: Int): Boolean =
        isOver(mouseX, mouseY) || (focused != null && WidgetTree.find(this, focused.id) === focused)

    /**
     * Draws the triggers and asks for the text to be shown while they are hovered or focused.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        super.render(ui, context, mouseX, mouseY)
        if (text.isNotEmpty() && wantsTooltip(context.focusedWidget, mouseX, mouseY)) context.showTooltip(text, bounds, side)
    }

    /**
     * Replaces the text.
     *
     * @param json the new text as component JSON
     */
    override fun applyText(json: String) {
        text = json
    }
}

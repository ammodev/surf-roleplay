package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeTokens
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.AlertVariant
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextKind

/**
 * A bordered callout on the card colour. Its content is stacked beside an optional icon, and its
 * texts take the colours of its variant.
 *
 * @param id the id of the widget
 * @property variant the look of the alert
 * @property icon the Lucide name of the icon, or `null` for none
 */
class AlertWidget(id: String, val variant: AlertVariant, val icon: String?) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = CONTENT_GAP
        crossAlign = Align.STRETCH
    }

    /**
     * Sets the padding that leaves room for the icon, then creates the layout box of the alert.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        padding = Insets(PADDING_Y, PADDING_X, PADDING_Y, PADDING_X + if (icon != null) ICON_SPACE else 0)
        return super.createLayout(measurer)
    }

    /**
     * Draws the surface, the border and the icon, then the content with the texts in the
     * variant's colours.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        ui.fillRounded(bounds, tokens.card)
        ui.borderRounded(bounds, tokens.border)
        icon?.let {
            val color = if (variant == AlertVariant.DESTRUCTIVE) tokens.destructive else tokens.cardForeground
            ui.icon(it, Rect(bounds.x + PADDING_X, bounds.y + PADDING_Y, UiMetrics.INLINE_ICON, UiMetrics.INLINE_ICON), color)
        }
        WidgetTree.visit(this) { if (it is TextWidget) it.tint = textColor(tokens, variant, it.kind) }
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Holds the alert metrics and colours.
     */
    companion object {
        /**
         * The space left and right inside the alert.
         */
        const val PADDING_X: Int = 8

        /**
         * The space above and below the content.
         */
        const val PADDING_Y: Int = 6

        /**
         * The width of the icon column: the icon and the gap after it.
         */
        const val ICON_SPACE: Int = UiMetrics.INLINE_ICON + 6

        /**
         * The space between the parts of the content.
         */
        private const val CONTENT_GAP: Int = 2

        /**
         * The opacity of a destructive description, relative to the destructive colour.
         */
        private const val DESCRIPTION_ALPHA: Float = 0.9f

        /**
         * Returns the colour of a text inside an alert.
         *
         * @param tokens the theme tokens
         * @param variant the variant of the alert
         * @param kind the kind of the text
         * @return the colour
         */
        fun textColor(tokens: ThemeTokens, variant: AlertVariant, kind: TextKind): Int = when {
            variant == AlertVariant.DESTRUCTIVE && kind == TextKind.ALERT_DESCRIPTION -> ThemeColors.withAlpha(tokens.destructive, DESCRIPTION_ALPHA)
            variant == AlertVariant.DESTRUCTIVE -> tokens.destructive
            kind == TextKind.ALERT_DESCRIPTION -> tokens.mutedForeground
            else -> tokens.cardForeground
        }
    }
}

/**
 * A bordered surface on the card colour that stacks a header, content and footer.
 *
 * @param id the id of the widget
 */
class CardWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = SECTION_GAP
        padding = Insets(PADDING_Y, 0, PADDING_Y, 0)
        crossAlign = Align.STRETCH
    }

    /**
     * Draws a faint shadow, the surface and the border, then the content.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val radius = tokens.radius + RADIUS_EXTRA
        ui.fillRounded(bounds.copy(y = bounds.y + 1), ThemeColors.withAlpha(SHADOW, SHADOW_ALPHA), radius)
        ui.fillRounded(bounds, tokens.card, radius)
        ui.borderRounded(bounds, tokens.border, radius)
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Holds the card metrics.
     */
    companion object {
        /**
         * The space left and right of the card sections.
         */
        const val PADDING_X: Int = 12

        /**
         * The space above the first and below the last section.
         */
        const val PADDING_Y: Int = 12

        /**
         * The space between two sections.
         */
        const val SECTION_GAP: Int = 12

        /**
         * How much rounder a card is than the theme radius.
         */
        private const val RADIUS_EXTRA: Int = 2

        /**
         * The colour of the shadow below a card.
         */
        private const val SHADOW: Int = -0x1000000

        /**
         * The opacity of the shadow.
         */
        private const val SHADOW_ALPHA: Float = 0.15f
    }
}

/**
 * The header of a card. Its title, description and other texts are stacked, and an action is
 * placed at its top right beside them.
 *
 * @param id the id of the widget
 */
class CardHeaderWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL) {

    /**
     * Creates a layout of a growing stack of the texts and the action beside it, with the card's
     * side padding.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        val action = childList.firstOrNull { it is CardActionWidget }
        val texts = LayoutBox(
            width = Sizing.grow(),
            axis = Axis.VERTICAL,
            gap = TEXT_GAP,
            crossAlign = Align.STRETCH,
            children = childList.filter { it !== action }.map { it.createLayout(measurer) },
        )
        return LayoutBox(
            width = width,
            height = height,
            axis = Axis.HORIZONTAL,
            gap = ACTION_GAP,
            padding = Insets(0, CardWidget.PADDING_X, 0, CardWidget.PADDING_X),
            children = listOfNotNull(texts, action?.createLayout(measurer)),
        ).also { layoutBox = it }
    }

    /**
     * Holds the header metrics.
     */
    companion object {
        /**
         * The space between the texts and the action.
         */
        const val ACTION_GAP: Int = 8

        /**
         * The space between two texts.
         */
        const val TEXT_GAP: Int = 4
    }
}

/**
 * The action of a card header.
 *
 * @param id the id of the widget
 */
class CardActionWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL)

/**
 * The content of a card, stacked across the card's width inside its side padding.
 *
 * @param id the id of the widget
 */
class CardContentWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        padding = Insets(0, CardWidget.PADDING_X, 0, CardWidget.PADDING_X)
        crossAlign = Align.STRETCH
    }
}

/**
 * The footer of a card: a row centered on one line inside the card's side padding.
 *
 * @param id the id of the widget
 */
class CardFooterWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = FOOTER_GAP
        padding = Insets(0, CardWidget.PADDING_X, 0, CardWidget.PADDING_X)
        crossAlign = Align.CENTER
    }

    /**
     * Holds the footer gap.
     */
    private companion object {
        /**
         * The space between two parts of the footer.
         */
        const val FOOTER_GAP: Int = 8
    }
}

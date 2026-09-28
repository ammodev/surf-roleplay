package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.EmptyMediaVariant
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.ItemMediaVariant
import dev.slne.surf.roleplay.protocol.screen.ItemSize
import dev.slne.surf.roleplay.protocol.screen.ItemVariant
import dev.slne.surf.roleplay.protocol.screen.SizeMode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextKind
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * An empty state: a centered stack of a header and content, optionally inside a dashed border.
 *
 * @param id the id of the widget
 * @property outline whether a dashed border is drawn around the state
 */
class EmptyWidget(id: String, val outline: Boolean) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = GAP
        padding = Insets(GAP, GAP, GAP, GAP)
        mainAlign = Align.CENTER
        crossAlign = Align.CENTER
    }

    /**
     * Draws the dashed border, if any, then the content.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        if (outline) ui.dashedBorder(bounds, ui.tokens.border)
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Holds the spacing.
     */
    private companion object {
        /**
         * The padding of the state and the space between its header and content.
         */
        const val GAP: Int = 12
    }
}

/**
 * The header of an empty state: its media, title and description stacked and centered.
 *
 * @param id the id of the widget
 */
class EmptyHeaderWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = 4
        crossAlign = Align.CENTER
    }
}

/**
 * The media of an empty state: its content as it is, or an icon on a muted rounded square.
 *
 * @param id the id of the widget
 * @property variant the look of the media
 * @property icon the Lucide name of the icon of the icon variant, or `null` for none
 */
class EmptyMediaWidget(id: String, val variant: EmptyMediaVariant, val icon: String?) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = 4
        crossAlign = Align.CENTER
        padding = Insets(bottom = MARGIN)
    }

    /**
     * Creates a square box for the icon variant, or the box of the content otherwise.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox =
        if (variant == EmptyMediaVariant.ICON) {
            LayoutBox(width = width, height = height, content = Size(ICON_BOX, ICON_BOX + MARGIN)).also { layoutBox = it }
        } else {
            super.createLayout(measurer)
        }

    /**
     * Draws the icon on its muted square for the icon variant, or the content otherwise.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        if (variant != EmptyMediaVariant.ICON) {
            super.render(ui, context, mouseX, mouseY)
            return
        }
        val tokens = ui.tokens
        val box = Rect(bounds.x + (bounds.width - ICON_BOX) / 2, bounds.y, ICON_BOX, ICON_BOX)
        ui.fillRounded(box, tokens.muted)
        icon?.let { ui.icon(it, Rect(box.x + (ICON_BOX - ICON) / 2, box.y + (ICON_BOX - ICON) / 2, ICON, ICON), tokens.foreground) }
    }

    /**
     * Holds the media metrics.
     */
    companion object {
        /**
         * The side of the muted square of the icon variant.
         */
        const val ICON_BOX: Int = 20

        /**
         * The side of the icon of the icon variant.
         */
        private const val ICON: Int = 12

        /**
         * The space below the media.
         */
        private const val MARGIN: Int = 4
    }
}

/**
 * The content of an empty state, such as buttons, stacked and centered.
 *
 * @param id the id of the widget
 */
class EmptyContentWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = 8
        crossAlign = Align.CENTER
    }
}

/**
 * A row of media, content and actions, with its headers above and its footers below. A clickable
 * item highlights under the mouse, can take the focus, and fires a widget action that does not
 * submit input when it is clicked outside its own controls or activated with Enter or Space.
 *
 * @param id the id of the widget
 * @property variant the look of the item
 * @property size the size of the item
 * @property clickable whether the item fires widget actions
 */
class ItemWidget(id: String, val variant: ItemVariant, val size: ItemSize, val clickable: Boolean) : ContainerWidget(id, Axis.VERTICAL) {

    /**
     * Whether the item can take the keyboard focus: while it is clickable and enabled.
     */
    override val focusable: Boolean get() = clickable && enabled

    /**
     * Creates a stack of the headers, a row of the other parts, and the footers, with the size's
     * padding and gaps. The first content part grows to take the space the others leave.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        childList.firstOrNull { it is ItemContentWidget }?.let { if (it.width.mode == SizeMode.FIT) it.width = Sizing.grow() }
        val headers = childList.filterIsInstance<ItemHeaderWidget>()
        val footers = childList.filterIsInstance<ItemFooterWidget>()
        val rowParts = childList.filter { it !is ItemHeaderWidget && it !is ItemFooterWidget }
        val hasDescription = rowParts.any { part -> var found = false; WidgetTree.visit(part) { if (it is TextWidget && it.kind == TextKind.ITEM_DESCRIPTION) found = true }; found }
        val row = if (rowParts.isEmpty()) {
            null
        } else {
            LayoutBox(
                axis = Axis.HORIZONTAL,
                gap = gap(size),
                crossAlign = if (hasDescription) Align.START else Align.CENTER,
                children = rowParts.map { it.createLayout(measurer) },
            )
        }
        return LayoutBox(
            width = width,
            height = height,
            axis = Axis.VERTICAL,
            gap = gap(size),
            padding = padding(size),
            crossAlign = Align.STRETCH,
            children = headers.map { it.createLayout(measurer) } + listOfNotNull(row) + footers.map { it.createLayout(measurer) },
        ).also { layoutBox = it }
    }

    /**
     * Draws the variant's border or background, highlighted under the mouse when clickable, then
     * the parts.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        when {
            clickable && enabled && isOver(mouseX, mouseY) -> ui.fillRounded(bounds, ThemeColors.withAlpha(tokens.accent, HALF))
            variant == ItemVariant.MUTED -> ui.fillRounded(bounds, ThemeColors.withAlpha(tokens.muted, HALF))
        }
        if (variant == ItemVariant.OUTLINE) ui.borderRounded(bounds, tokens.border)
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Passes a click to the parts first; a left click that no part handled fires the action of a
     * clickable item.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was handled
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (super.mouseClicked(context, x, y, button)) return true
        if (!clickable) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            context.actionTriggered(this, false)
        }
        return true
    }

    /**
     * Fires the action of a clickable item on Enter or Space.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!clickable || !enabled || !isActivation(event)) return false
        context.actionTriggered(this, false)
        return true
    }

    /**
     * Holds the item metrics.
     */
    companion object {
        /**
         * The opacity of the muted background and of the hover highlight.
         */
        private const val HALF: Float = 0.5f

        /**
         * Returns the padding of an item size.
         *
         * @param size the size
         * @return the padding
         */
        fun padding(size: ItemSize): Insets = when (size) {
            ItemSize.DEFAULT -> Insets(8, 8, 8, 8)
            ItemSize.SM -> Insets(6, 8, 6, 8)
        }

        /**
         * Returns the space between the parts of an item size.
         *
         * @param size the size
         * @return the gap
         */
        fun gap(size: ItemSize): Int = when (size) {
            ItemSize.DEFAULT -> 8
            ItemSize.SM -> 5
        }
    }
}

/**
 * The media at the start of an item: its content as it is, an icon on a small muted bordered
 * square, or its content clipped to a square.
 *
 * @param id the id of the widget
 * @property variant the look of the media
 * @property icon the Lucide name of the icon of the icon variant, or `null` for none
 */
class ItemMediaWidget(id: String, val variant: ItemMediaVariant, val icon: String?) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = 4
        crossAlign = Align.CENTER
    }

    /**
     * Creates a square box for the icon variant, a square that its content fills for the image
     * variant, or the box of the content otherwise.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox = when (variant) {
        ItemMediaVariant.ICON -> LayoutBox(width = width, height = height, content = Size(ICON_BOX, ICON_BOX)).also { layoutBox = it }
        ItemMediaVariant.IMAGE -> LayoutBox(
            width = Sizing.fixed(IMAGE_BOX),
            axis = Axis.VERTICAL,
            children = childList.map { it.createLayout(measurer) },
            aspectRatio = 1f,
        ).also { layoutBox = it }
        ItemMediaVariant.DEFAULT -> super.createLayout(measurer)
    }

    /**
     * Draws the icon on its square, the content clipped to a rounded square, or the content as
     * it is.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        when (variant) {
            ItemMediaVariant.DEFAULT -> super.render(ui, context, mouseX, mouseY)
            ItemMediaVariant.IMAGE -> ui.clippedRound(bounds, RADIUS) { super.render(ui, context, mouseX, mouseY) }
            ItemMediaVariant.ICON -> {
                val tokens = ui.tokens
                val box = Rect(bounds.x, bounds.y + (bounds.height - ICON_BOX) / 2, ICON_BOX, ICON_BOX)
                ui.fillRounded(box, tokens.muted, RADIUS)
                ui.borderRounded(box, tokens.border, RADIUS)
                val side = UiMetrics.INLINE_ICON - 2
                icon?.let { ui.icon(it, Rect(box.x + (ICON_BOX - side) / 2, box.y + (ICON_BOX - side) / 2, side, side), tokens.foreground) }
            }
        }
    }

    /**
     * Holds the media metrics.
     */
    companion object {
        /**
         * The side of the square of the icon variant.
         */
        const val ICON_BOX: Int = 16

        /**
         * The side of the square of the image variant.
         */
        const val IMAGE_BOX: Int = 20

        /**
         * The corner radius of the squares.
         */
        private const val RADIUS: Int = 2
    }
}

/**
 * The stacked title and description of an item.
 *
 * @param id the id of the widget
 */
class ItemContentWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = 2
        crossAlign = Align.STRETCH
    }
}

/**
 * The actions at the end of an item, in a row centered on one line.
 *
 * @param id the id of the widget
 */
class ItemActionsWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = 4
        crossAlign = Align.CENTER
    }
}

/**
 * A line above or below the row of an item. Its first part is placed at the start and the others
 * at the end.
 *
 * @param id the id of the widget
 */
open class ItemLineWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = 4
        crossAlign = Align.CENTER
    }

    /**
     * Creates a row of the parts with a growing space after the first one.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        val boxes = childList.map { it.createLayout(measurer) }.toMutableList()
        if (boxes.size > 1) boxes.add(1, LayoutBox(width = Sizing.grow()))
        return LayoutBox(width = width, height = height, axis = Axis.HORIZONTAL, gap = gap, crossAlign = crossAlign, children = boxes).also { layoutBox = it }
    }
}

/**
 * A line above the row of an item.
 *
 * @param id the id of the widget
 */
class ItemHeaderWidget(id: String) : ItemLineWidget(id)

/**
 * A line below the row of an item.
 *
 * @param id the id of the widget
 */
class ItemFooterWidget(id: String) : ItemLineWidget(id)

/**
 * A stack of items and separators, each as wide as the group.
 *
 * @param id the id of the widget
 */
class ItemGroupWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }
}

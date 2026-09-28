package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ButtonVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * A widget that fires an action when clicked with the left button or activated with Enter or
 * Space, while it is enabled.
 *
 * @param id the id of the widget
 */
abstract class ClickableWidget(id: String) : Widget(id) {

    /**
     * Whether the widget can take the focus: while it is enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * Focuses the widget and fires its action on a left click.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on the widget
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            context.actionTriggered(this, submitsInput = false)
        }
        return true
    }

    /**
     * Fires the action on Enter or Space.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled || !isActivation(event)) return false
        context.actionTriggered(this, submitsInput = false)
        return true
    }
}

/**
 * A row of widgets centered across its cross axis, used for the lists and items of breadcrumbs
 * and paginations.
 *
 * @param id the id of the widget
 * @param gap the space between the widgets
 * @param mainAlign where the widgets are placed along the row
 */
class PathRowWidget(id: String, gap: Int, mainAlign: Align = Align.START) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        this.gap = gap
        this.mainAlign = mainAlign
        crossAlign = Align.CENTER
    }
}

/**
 * A link of a breadcrumb: muted text that turns to the foreground colour when hovered.
 *
 * @param id the id of the widget
 * @param text the text as component JSON
 */
class BreadcrumbLinkWidget(id: String, var text: String) : ClickableWidget(id) {

    /**
     * Returns the size of the text.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(measurer.width(text), measurer.lineHeight)

    /**
     * Draws the text, in the foreground colour while hovered.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val color = if (enabled && isOver(mouseX, mouseY)) ui.tokens.foreground else ui.tokens.mutedForeground
        ui.text(text, bounds.x, bounds.y, if (enabled) color else ui.disabled(color))
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

/**
 * The current page of a breadcrumb, in the foreground colour.
 *
 * @param id the id of the widget
 * @param text the text as component JSON
 */
class BreadcrumbPageWidget(id: String, var text: String) : Widget(id) {

    /**
     * Returns the size of the text.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(measurer.width(text), measurer.lineHeight)

    /**
     * Draws the text.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.text(text, bounds.x, bounds.y, ui.tokens.foreground)
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

/**
 * An icon in a muted colour, such as a breadcrumb separator.
 *
 * @param id the id of the widget
 * @property icon the Lucide name of the icon
 * @property side the width and height of the icon
 */
class MutedIconWidget(id: String, val icon: String, val side: Int) : Widget(id) {

    /**
     * Returns the square of the icon.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(side, side)

    /**
     * Draws the icon.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.icon(icon, bounds, ui.tokens.mutedForeground)
    }
}

/**
 * An ellipsis in a square box: a muted icon that fires an action when clicked, such as the
 * trigger of a dropdown menu with collapsed breadcrumb items.
 *
 * @param id the id of the widget
 * @property box the width and height of the box
 * @property clickable whether the ellipsis can be clicked and focused
 */
class EllipsisWidget(id: String, val box: Int, val clickable: Boolean) : ClickableWidget(id) {

    /**
     * Whether the ellipsis can take the focus: while it is clickable and enabled.
     */
    override val focusable: Boolean get() = clickable && enabled

    /**
     * Returns the square of the box.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(box, box)

    /**
     * Draws the icon centered in the box.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val color = if (clickable && isOver(mouseX, mouseY)) ui.tokens.foreground else ui.tokens.mutedForeground
        ui.icon("ellipsis", Rect(bounds.x + (bounds.width - ICON) / 2, bounds.y + (bounds.height - ICON) / 2, ICON, ICON), color)
    }

    /**
     * Fires the action on a left click if the ellipsis is clickable.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on the ellipsis
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean =
        if (clickable) super.mouseClicked(context, x, y, button) else isOver(x, y)

    /**
     * Holds the ellipsis metrics.
     */
    companion object {
        /**
         * The size of the icon.
         */
        const val ICON: Int = 8
    }
}

/**
 * What a pagination link leads to.
 */
enum class PaginationLinkKind {
    /**
     * A page; its text is the page number.
     */
    PAGE,

    /**
     * The previous page; a chevron before the text.
     */
    PREVIOUS,

    /**
     * The next page; a chevron after the text.
     */
    NEXT,
}

/**
 * A link of a pagination, drawn like a ghost button, or like an outline button while it is the
 * current page.
 *
 * @param id the id of the widget
 * @param text the text as component JSON
 * @property active whether the link is the current page
 * @property kind what the link leads to
 */
class PaginationLinkWidget(id: String, var text: String, val active: Boolean, val kind: PaginationLinkKind) : ClickableWidget(id) {

    /**
     * Returns the size: at least a square for page links, and the chevron, text and padding for
     * the previous and next links.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        val text = measurer.width(text)
        return if (kind == PaginationLinkKind.PAGE) Size(maxOf(HEIGHT, text + 2 * PADDING_X), HEIGHT)
        else Size(PADDING_X + ICON + ICON_GAP + text + PADDING_X, HEIGHT)
    }

    /**
     * Draws the link with the colours of a ghost or outline button, its text and its chevron.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val colors = ButtonStyle.colors(ui.tokens, if (active) ButtonVariant.OUTLINE else ButtonVariant.GHOST, enabled && isOver(mouseX, mouseY))
        val fade: (Int) -> Int = { if (enabled) it else ui.disabled(it) }
        colors.background?.let { ui.fillRounded(bounds, fade(it)) }
        colors.border?.let { ui.borderRounded(bounds, fade(it)) }
        val color = fade(colors.foreground)
        val textY = bounds.y + (bounds.height - ui.lineHeight + 1) / 2
        val iconY = bounds.y + (bounds.height - ICON) / 2
        when (kind) {
            PaginationLinkKind.PAGE -> ui.centeredText(text, bounds, color)
            PaginationLinkKind.PREVIOUS -> {
                ui.icon("chevron-left", Rect(bounds.x + PADDING_X, iconY, ICON, ICON), color)
                ui.text(text, bounds.x + PADDING_X + ICON + ICON_GAP, textY, color)
            }
            PaginationLinkKind.NEXT -> {
                ui.text(text, bounds.x + PADDING_X, textY, color)
                ui.icon("chevron-right", Rect(bounds.right - PADDING_X - ICON, iconY, ICON, ICON), color)
            }
        }
    }

    /**
     * Replaces the text.
     *
     * @param json the new text as component JSON
     */
    override fun applyText(json: String) {
        text = json
    }

    /**
     * Holds the link metrics.
     */
    companion object {
        /**
         * The height of a link.
         */
        const val HEIGHT: Int = 20

        /**
         * The space left and right of the content.
         */
        const val PADDING_X: Int = 6

        /**
         * The size of the chevron.
         */
        const val ICON: Int = 8

        /**
         * The space between the chevron and the text.
         */
        const val ICON_GAP: Int = 2
    }
}

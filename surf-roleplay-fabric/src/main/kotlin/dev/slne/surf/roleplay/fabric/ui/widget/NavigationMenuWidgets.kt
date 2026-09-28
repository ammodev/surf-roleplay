package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * A navigation menu: its list of items, laid out in a row.
 *
 * @param id the id of the widget
 */
class NavigationMenuWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        crossAlign = Align.CENTER
    }

    /**
     * The items of every list of the menu, in order.
     */
    val items: List<NavigationMenuItemWidget>
        get() = childList.flatMap { list -> (list as? ContainerWidget)?.childList.orEmpty() }.filterIsInstance<NavigationMenuItemWidget>()
}

/**
 * An item of a navigation menu: a trigger with a content that opens below it, or a link. The
 * content opens on a click of the trigger, or after the mouse rests on the trigger; content
 * opened by hovering closes shortly after the mouse left the trigger and the content, content
 * opened by a click stays until a click outside or Escape. While the content of one item is open,
 * pointing at another item's trigger switches to it.
 *
 * @param id the id of the widget
 */
class NavigationMenuItemWidget(id: String) : OverlayHostWidget(id) {

    /**
     * The menu the item belongs to, set by the factory.
     */
    var menu: NavigationMenuWidget? = null

    /**
     * The timing of opening and closing by hover.
     */
    private val timer: HoverTimer = HoverTimer(OPEN_DELAY, CLOSE_DELAY)

    /**
     * Whether the open content was opened by hovering, so that leaving closes it.
     */
    private var byHover: Boolean = false

    /**
     * When the mouse was last on the open content, in milliseconds.
     */
    private var contentSeen: Long = Long.MIN_VALUE / 2

    /**
     * Actions inside the item toggle its content only if it has one; a link directly in the item
     * fires its own action.
     */
    override val togglesOnTriggerAction: Boolean get() = content != null

    /**
     * Creates the overlay that shows the content below the trigger, records when the mouse is on
     * it, and closes after a link in it fired.
     *
     * @return the overlay
     */
    override fun createPopover(): Popover = object : WidgetPopover(this, OverlaySide.BOTTOM, Align.START) {
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

        /**
         * Closes the content after a widget in it fired its action.
         *
         * @param context the screen showing the overlay
         * @param widget the widget
         * @param submitsInput whether the action submitted the screen's input
         */
        override fun afterAction(context: UiContext, widget: Widget, submitsInput: Boolean) = context.closePopover(this)
    }

    /**
     * Follows the mouse: switches to this item if another item of the menu is open and the mouse
     * is on this trigger, and otherwise opens and closes the content by hover.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param now the time in milliseconds
     */
    fun pointer(context: UiContext, x: Int, y: Int, now: Long) {
        if (content == null || !enabled) return
        if (!open) byHover = false
        val onTrigger = isOver(x, y)
        if (!open && onTrigger) {
            val other = menu?.items?.firstOrNull { it !== this && it.open }
            if (other != null) {
                other.close(context)
                show(context, report = true)
                byHover = true
                timer.update(now, true)
                return
            }
        }
        val hovered = onTrigger || (open && now - contentSeen < FRAME_TOLERANCE_MILLIS)
        val wanted = timer.update(now, hovered)
        if (wanted && !open) {
            show(context, report = true)
            byHover = true
        }
        if (!wanted && open && byHover) close(context)
    }

    /**
     * Draws the trigger and follows the mouse.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        super.render(ui, context, mouseX, mouseY)
        pointer(context, mouseX, mouseY, System.currentTimeMillis())
    }

    /**
     * Holds the hover timing.
     */
    companion object {
        /**
         * How long the mouse rests on a trigger before its content opens, in milliseconds.
         */
        const val OPEN_DELAY: Int = 200

        /**
         * How long after the mouse left the trigger and the content the content closes, in
         * milliseconds.
         */
        const val CLOSE_DELAY: Int = 150

        /**
         * How long the mouse counts as on the content after it was last seen there.
         */
        private const val FRAME_TOLERANCE_MILLIS: Long = 100
    }
}

/**
 * The trigger of a navigation menu item: its text and a small chevron that points up while the
 * content is open, on a background that is highlighted while hovered or open.
 *
 * @param id the id of the widget
 * @param text the text as component JSON
 */
class NavigationMenuTriggerWidget(id: String, var text: String) : ClickableWidget(id) {

    /**
     * The item this trigger opens, set by the factory.
     */
    var item: NavigationMenuItemWidget? = null

    /**
     * Returns the size of the text and the chevron with padding.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(NavigationMenuStyle.PADDING_X + measurer.width(text) + GAP + CHEVRON + NavigationMenuStyle.PADDING_X, NavigationMenuStyle.HEIGHT)

    /**
     * Draws the background, the text and the chevron.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val open = item?.open == true
        NavigationMenuStyle.background(ui, bounds, enabled && isOver(mouseX, mouseY), open)
        val color = if (enabled) ui.tokens.foreground else ui.disabled(ui.tokens.foreground)
        ui.text(text, bounds.x + NavigationMenuStyle.PADDING_X, bounds.y + (bounds.height - ui.lineHeight + 1) / 2, color)
        val chevron = Rect(bounds.right - NavigationMenuStyle.PADDING_X - CHEVRON, bounds.y + (bounds.height - CHEVRON) / 2 + 1, CHEVRON, CHEVRON)
        ui.rotatedIcon("chevron-down", chevron, color, if (open) 180f else 0f)
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
     * Holds the trigger metrics.
     */
    private companion object {
        /**
         * The space between the text and the chevron.
         */
        const val GAP: Int = 2

        /**
         * The size of the chevron.
         */
        const val CHEVRON: Int = 6
    }
}

/**
 * The content of a navigation menu item: a padded stack on a bordered surface below the trigger.
 *
 * @param id the id of the widget
 */
class NavigationMenuContentWidget(id: String) : OverlayContentWidget(id) {
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
    private companion object {
        /**
         * The space inside the surface.
         */
        const val PADDING: Int = 4

        /**
         * The space between two links.
         */
        const val GAP: Int = 2
    }
}

/**
 * A link of a navigation menu: its content, stacked, on a background that is highlighted while
 * hovered or active. A click anywhere on it fires its action. Directly in an item it is drawn
 * like a trigger.
 *
 * @param id the id of the widget
 * @property active whether the link leads to the current page
 * @property triggerStyle whether the link is drawn like a trigger
 */
class NavigationMenuLinkWidget(id: String, val active: Boolean, val triggerStyle: Boolean) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = GAP
        crossAlign = Align.STRETCH
        padding = if (triggerStyle) Insets(TRIGGER_PADDING_Y, NavigationMenuStyle.PADDING_X, TRIGGER_PADDING_Y, NavigationMenuStyle.PADDING_X) else Insets(PADDING, PADDING, PADDING, PADDING)
    }

    /**
     * Whether the link can take the focus: while it is enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * The link is focused as a whole, not through its content.
     */
    override val focusChildren: List<Widget> get() = emptyList()

    /**
     * Draws the background and the content.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val hovered = enabled && isOver(mouseX, mouseY)
        if (triggerStyle) NavigationMenuStyle.background(ui, bounds, hovered, active)
        else if (hovered) ui.fillRounded(bounds, ui.tokens.accent, 2)
        else if (active) ui.fillRounded(bounds, ThemeColors.withAlpha(ui.tokens.accent, ACTIVE_ALPHA), 2)
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Focuses the link and fires its action on a left click anywhere on it.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on the link
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

    /**
     * Holds the link metrics.
     */
    private companion object {
        /**
         * The space inside a link in a content.
         */
        const val PADDING: Int = 4

        /**
         * The space above and below the content of a link drawn like a trigger.
         */
        const val TRIGGER_PADDING_Y: Int = 5

        /**
         * The space between the parts of the content.
         */
        const val GAP: Int = 1

        /**
         * The opacity of the background of an active link.
         */
        const val ACTIVE_ALPHA: Float = 0.5f
    }
}

/**
 * The shared look of navigation menu triggers and of links drawn like them.
 */
object NavigationMenuStyle {
    /**
     * The height of a trigger.
     */
    const val HEIGHT: Int = 18

    /**
     * The space left and right of a trigger's content.
     */
    const val PADDING_X: Int = 8

    /**
     * The opacity of the background of an open trigger.
     */
    private const val OPEN_ALPHA: Float = 0.5f

    /**
     * Draws the background of a trigger: accent while hovered, half accent while open or active,
     * and the background colour otherwise.
     *
     * @param ui the graphics to draw with
     * @param area the trigger's area
     * @param hovered whether the mouse is on it
     * @param open whether its content is open or it is active
     */
    fun background(ui: UiGraphics, area: Rect, hovered: Boolean, open: Boolean) {
        val color = when {
            hovered -> ui.tokens.accent
            open -> ThemeColors.withAlpha(ui.tokens.accent, OPEN_ALPHA)
            else -> ui.tokens.background
        }
        ui.fillRounded(area, color)
    }
}

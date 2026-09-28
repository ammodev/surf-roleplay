package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import dev.slne.surf.roleplay.protocol.screen.SidebarCollapsible
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuButtonSize
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuButtonVariant
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuSubButtonSize
import dev.slne.surf.roleplay.protocol.screen.SidebarSide
import dev.slne.surf.roleplay.protocol.screen.SidebarVariant
import dev.slne.surf.roleplay.protocol.screen.Sizing
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.abs

/**
 * The frame of a sidebar layout: a sidebar and the inset beside it in a row. Whether the sidebar
 * is expanded is its input value, `true` or `false`. Sidebar triggers and rails inside it and
 * Ctrl+B anywhere on the screen expand and collapse the sidebar, unless it cannot collapse.
 *
 * @param id the id of the widget
 */
class SidebarProviderWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL), ActionInterceptor, ShortcutWidget {
    init {
        crossAlign = Align.STRETCH
    }

    /**
     * Whether the sidebar is expanded.
     */
    var open: Boolean = true
        private set

    /**
     * The sidebar of the provider, or `null` if it has none.
     */
    val sidebar: SidebarWidget? get() = childList.firstOrNull { it is SidebarWidget } as SidebarWidget?

    /**
     * Whether the sidebar is expanded, as `true` or `false`.
     */
    override val inputValue: String get() = open.toString()

    /**
     * Expands or collapses the sidebar as the server asks, without reporting the change.
     *
     * @param value `true` to expand it, anything else to collapse it
     */
    override fun applyValue(value: String) = setOpen(value == "true")

    /**
     * Expands or collapses the sidebar.
     *
     * @param open whether the sidebar is expanded
     */
    fun setOpen(open: Boolean) {
        this.open = open
        sidebar?.applyState(open)
        childList.filterIsInstance<SidebarInsetWidget>().forEach { it.sidebar = sidebar }
    }

    /**
     * Toggles the sidebar unless it cannot collapse, and reports the change.
     *
     * @param context the screen showing the widget
     */
    fun toggle(context: UiContext) {
        if (!enabled || sidebar?.collapsible == SidebarCollapsible.NONE) return
        setOpen(!open)
        markChanged(context, immediate = true)
        context.requestLayout()
    }

    /**
     * Toggles the sidebar when a sidebar trigger or rail inside the provider fires.
     *
     * @param context the screen showing the widget
     * @param widget the widget whose action fired
     * @param via the child holding the widget
     * @return whether the action came from a trigger or rail
     */
    override fun interceptAction(context: UiContext, widget: Widget, via: Widget): Boolean {
        if (widget !is SidebarTriggerWidget && widget !is SidebarRailWidget) return false
        toggle(context)
        return true
    }

    /**
     * Toggles the sidebar on Ctrl+B.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was the shortcut
     */
    override fun shortcut(context: UiContext, event: KeyEvent): Boolean {
        if (event.key() != GLFW.GLFW_KEY_B || event.modifiers() and GLFW.GLFW_MOD_CONTROL == 0) return false
        toggle(context)
        return true
    }

    /**
     * Draws the sidebar colour behind an inset sidebar and its inset, then the children.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        if (sidebar?.variant == SidebarVariant.INSET) ui.fill(bounds, SidebarStyle.background(ui))
        super.render(ui, context, mouseX, mouseY)
    }
}

/**
 * The shared colours of sidebars.
 */
object SidebarStyle {
    /**
     * Returns the background colour of a sidebar.
     *
     * @param ui the graphics to draw with
     * @return the colour
     */
    fun background(ui: UiGraphics): Int = ui.tokens.card
}

/**
 * A sidebar: a column of header, content and footer on one side of its provider. Expanded it
 * takes its full width. Collapsed, an offcanvas sidebar disappears and a sidebar collapsible to
 * icons shrinks to the icons of its menu buttons, hiding its labels, actions, badges and
 * sub-menus. A rail inside it is placed along its inner edge.
 *
 * @param id the id of the widget
 * @property side the side the sidebar is on
 * @property variant how the sidebar is drawn
 * @property collapsible how the sidebar collapses
 */
class SidebarWidget(id: String, val side: SidebarSide, val variant: SidebarVariant, val collapsible: SidebarCollapsible) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
        if (variant != SidebarVariant.SIDEBAR) padding = Insets(MARGIN, MARGIN, MARGIN, MARGIN)
        width = Sizing.fixed(WIDTH)
    }

    /**
     * Whether the sidebar is collapsed to its icons.
     */
    var iconOnly: Boolean = false
        private set

    /**
     * The side of a menu button the tooltip of a collapsed sidebar is shown on.
     */
    val tooltipSide: OverlaySide get() = if (side == SidebarSide.LEFT) OverlaySide.RIGHT else OverlaySide.LEFT

    /**
     * Applies the expanded state: the width, whether the sidebar is shown, and the icon mode of
     * its parts.
     *
     * @param open whether the sidebar is expanded
     */
    fun applyState(open: Boolean) {
        val expanded = open || collapsible == SidebarCollapsible.NONE
        iconOnly = !expanded && collapsible == SidebarCollapsible.ICON
        hidden = !expanded && collapsible == SidebarCollapsible.OFFCANVAS
        val margin = if (variant == SidebarVariant.SIDEBAR) 0 else 2 * MARGIN
        width = Sizing.fixed(if (iconOnly) ICON_WIDTH + margin else WIDTH)
        WidgetTree.visit(this) { part ->
            when (part) {
                is SidebarMenuButtonWidget -> {
                    part.iconOnly = iconOnly
                    part.tooltipSide = tooltipSide
                }
                is SidebarGroupLabelWidget, is SidebarGroupActionWidget, is SidebarMenuActionWidget, is SidebarMenuBadgeWidget, is SidebarMenuSubWidget -> part.hidden = iconOnly
                else -> Unit
            }
        }
    }

    /**
     * Creates the layout box of the column without the rail, which is placed separately.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox = LayoutBox(
        width = width,
        height = height,
        axis = axis,
        children = shownChildren.filter { it !is SidebarRailWidget }.map { it.createLayout(measurer) },
        gap = gap,
        padding = padding,
        mainAlign = mainAlign,
        crossAlign = crossAlign,
    ).also { layoutBox = it }

    /**
     * Lays the column out and places the rail along the inner edge.
     */
    override fun applyLayout() {
        super.applyLayout()
        val edge = if (side == SidebarSide.LEFT) bounds.right else bounds.x
        childList.filterIsInstance<SidebarRailWidget>().forEach { it.bounds = Rect(edge - SidebarRailWidget.HALF, bounds.y, 2 * SidebarRailWidget.HALF, bounds.height) }
    }

    /**
     * Draws the sidebar surface for its variant and its parts.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        when (variant) {
            SidebarVariant.SIDEBAR -> {
                ui.fill(bounds, SidebarStyle.background(ui))
                val x = if (side == SidebarSide.LEFT) bounds.right - 1 else bounds.x
                ui.fill(Rect(x, bounds.y, 1, bounds.height), ui.tokens.border)
            }
            SidebarVariant.FLOATING -> {
                val card = Rect(bounds.x + MARGIN, bounds.y + MARGIN, bounds.width - 2 * MARGIN, bounds.height - 2 * MARGIN)
                OverlaySurface.draw(ui, card)
                ui.fillRounded(card, SidebarStyle.background(ui))
                ui.borderRounded(card, ui.tokens.border)
            }
            SidebarVariant.INSET -> Unit
        }
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Holds the sidebar metrics.
     */
    companion object {
        /**
         * The width of an expanded sidebar.
         */
        const val WIDTH: Int = 128

        /**
         * The width of a sidebar collapsed to its icons, without the margin of floating and inset
         * sidebars.
         */
        const val ICON_WIDTH: Int = 24

        /**
         * The margin around floating and inset sidebars.
         */
        const val MARGIN: Int = 4
    }
}

/**
 * The main content beside a sidebar, taking the rest of the provider. Beside an inset sidebar it
 * is drawn as a card with a margin; otherwise on the background colour.
 *
 * @param id the id of the widget
 */
class SidebarInsetWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }

    /**
     * The sidebar beside the inset, set by the provider.
     */
    var sidebar: SidebarWidget? = null
        set(value) {
            field = value
            padding = if (value?.variant == SidebarVariant.INSET) Insets(SidebarWidget.MARGIN, SidebarWidget.MARGIN, SidebarWidget.MARGIN, SidebarWidget.MARGIN) else Insets.NONE
        }

    /**
     * Draws the card or the background, then the content.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        if (sidebar?.variant == SidebarVariant.INSET) {
            val m = SidebarWidget.MARGIN
            val card = Rect(bounds.x + m, bounds.y + m, bounds.width - 2 * m, bounds.height - 2 * m)
            OverlaySurface.draw(ui, card)
            ui.fillRounded(card, ui.tokens.background, ui.tokens.radius + 2)
        } else {
            ui.fill(bounds, ui.tokens.background)
        }
        super.render(ui, context, mouseX, mouseY)
    }
}

/**
 * The header or footer of a sidebar: its content, stacked with padding.
 *
 * @param id the id of the widget
 */
class SidebarSectionWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
        gap = 4
        padding = Insets(4, 4, 4, 4)
    }
}

/**
 * A group of a sidebar: its label with its action, and its content, stacked with padding.
 *
 * @param id the id of the widget
 */
class SidebarGroupWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
        padding = Insets(4, 4, 4, 4)
    }
}

/**
 * The label of a sidebar group, in muted text.
 *
 * @param id the id of the widget
 * @param text the text as component JSON
 */
class SidebarGroupLabelWidget(id: String, var text: String) : Widget(id) {

    /**
     * Returns the size of the text with padding.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(measurer.width(text) + 2 * PADDING_X, HEIGHT)

    /**
     * Draws the text.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.clipped(bounds) { ui.text(text, bounds.x + PADDING_X, bounds.y + (bounds.height - ui.lineHeight + 1) / 2, ui.tokens.mutedForeground) }
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
     * Holds the label metrics.
     */
    companion object {
        /**
         * The height of the label.
         */
        const val HEIGHT: Int = 16

        /**
         * The space left and right of the text.
         */
        const val PADDING_X: Int = 4
    }
}

/**
 * A small square icon button, highlighted when hovered, such as the action of a sidebar group or
 * menu item.
 *
 * @param id the id of the widget
 * @property icon the Lucide name of the icon
 */
open class SidebarIconButtonWidget(id: String, val icon: String) : ClickableWidget(id) {

    /**
     * Returns the size of the button.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(SIZE, SIZE)

    /**
     * Whether the button is drawn in the current frame.
     *
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @return whether it is drawn
     */
    open fun visible(context: UiContext, mouseX: Int, mouseY: Int): Boolean = true

    /**
     * Draws the highlight while hovered and the icon.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        if (!visible(context, mouseX, mouseY)) return
        val hovered = enabled && isOver(mouseX, mouseY)
        if (hovered) ui.fillRounded(bounds, ui.tokens.accent, 2)
        val color = if (!enabled) ui.disabled(ui.tokens.foreground) else if (hovered) ui.tokens.accentForeground else ui.tokens.foreground
        ui.icon(icon, Rect(bounds.x + (SIZE - ICON) / 2, bounds.y + (SIZE - ICON) / 2, ICON, ICON), color)
    }

    /**
     * Holds the button metrics.
     */
    companion object {
        /**
         * The width and height of the button.
         */
        const val SIZE: Int = 10

        /**
         * The size of the icon.
         */
        const val ICON: Int = 8
    }
}

/**
 * The action at the end of a sidebar group label.
 *
 * @param id the id of the widget
 * @param icon the Lucide name of the icon
 */
class SidebarGroupActionWidget(id: String, icon: String) : SidebarIconButtonWidget(id, icon)

/**
 * The action at the end of a sidebar menu button, drawn always or only while its item is hovered
 * or focused.
 *
 * @param id the id of the widget
 * @param icon the Lucide name of the icon
 * @property showOnHover whether the action is drawn only while its item is hovered or focused
 */
class SidebarMenuActionWidget(id: String, icon: String, val showOnHover: Boolean) : SidebarIconButtonWidget(id, icon) {

    /**
     * The item the action belongs to, set by the factory.
     */
    var item: SidebarMenuItemWidget? = null

    /**
     * Whether the action is drawn: always, or while its item is hovered or holds the focus.
     *
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @return whether it is drawn
     */
    override fun visible(context: UiContext, mouseX: Int, mouseY: Int): Boolean {
        if (!showOnHover) return true
        val item = item ?: return true
        val focused = context.focusedWidget
        return item.isOver(mouseX, mouseY) || (focused != null && WidgetTree.find(item, focused.id) === focused)
    }
}

/**
 * An item of a sidebar menu: its button, with its action and badge over the end of the button,
 * and its sub-menu below.
 *
 * @param id the id of the widget
 */
class SidebarMenuItemWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
        gap = 2
    }

    /**
     * Whether a child is placed over the end of the button instead of in the column.
     *
     * @param child the child
     * @return whether it is an action or a badge
     */
    private fun overlays(child: Widget): Boolean = child is SidebarMenuActionWidget || child is SidebarMenuBadgeWidget

    /**
     * Creates the layout box of the column without the action and the badge, which get their own
     * boxes and are placed over the button.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        shownChildren.filter(::overlays).forEach { it.createLayout(measurer) }
        val button = childList.firstOrNull { it is SidebarMenuButtonWidget } as SidebarMenuButtonWidget?
        button?.reservedEnd = if (shownChildren.any(::overlays)) RESERVED_END else 0
        return LayoutBox(
            width = width,
            height = height,
            axis = axis,
            children = shownChildren.filterNot(::overlays).map { it.createLayout(measurer) },
            gap = gap,
            padding = padding,
            crossAlign = crossAlign,
        ).also { layoutBox = it }
    }

    /**
     * Lays the column out and places the action at the end of the button and the badge before
     * it.
     */
    override fun applyLayout() {
        super.applyLayout()
        val button = childList.firstOrNull { it is SidebarMenuButtonWidget }?.bounds ?: return
        var end = button.right - INSET
        childList.filterIsInstance<SidebarMenuActionWidget>().filter { !it.hidden }.forEach { action ->
            val size = SidebarIconButtonWidget.SIZE
            action.bounds = Rect(end - size, button.y + (button.height - size) / 2, size, size)
            end -= size + INSET
        }
        childList.filterIsInstance<SidebarMenuBadgeWidget>().filter { !it.hidden }.forEach { badge ->
            val size = badge.layoutBox?.content ?: Size.ZERO
            badge.bounds = Rect(end - size.width, button.y + (button.height - size.height) / 2, size.width, size.height)
            end -= size.width + INSET
        }
    }

    /**
     * Offers a click to the action over the button first, then to the other children.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether a child handled the click
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean =
        shownChildren.filterIsInstance<SidebarMenuActionWidget>().any { it.mouseClicked(context, x, y, button) } || super.mouseClicked(context, x, y, button)

    /**
     * Holds the item metrics.
     */
    private companion object {
        /**
         * The space between the end of the button and its action or badge.
         */
        const val INSET: Int = 2

        /**
         * The space a button leaves free at its end for an action or badge.
         */
        const val RESERVED_END: Int = 16
    }
}

/**
 * The button of a sidebar menu item: its icon and text, highlighted while hovered or active.
 * While its sidebar is collapsed to icons only the icon is drawn, on a square, and the tooltip is
 * shown beside it on hover.
 *
 * @param id the id of the widget
 * @param text the text as component JSON
 * @property icon the Lucide name of the icon, or `null` for none
 * @property size the size of the button
 * @property variant how the button is drawn
 * @property active whether the button leads to the current page
 * @property tooltip the tooltip as component JSON, or empty for none
 */
class SidebarMenuButtonWidget(
    id: String,
    var text: String,
    val icon: String?,
    val size: SidebarMenuButtonSize,
    val variant: SidebarMenuButtonVariant,
    val active: Boolean,
    val tooltip: String,
) : ClickableWidget(id) {

    /**
     * Whether only the icon is drawn, set by the sidebar while it is collapsed to icons.
     */
    var iconOnly: Boolean = false

    /**
     * The side of the button the tooltip is shown on, set by the sidebar.
     */
    var tooltipSide: OverlaySide = OverlaySide.RIGHT

    /**
     * The space the button leaves free at its end for an action or badge, set by its item.
     */
    var reservedEnd: Int = 0

    /**
     * Whether the tooltip is shown on hover: while only the icon is drawn and there is a tooltip.
     */
    val wantsTooltip: Boolean get() = iconOnly && tooltip.isNotEmpty()

    /**
     * The height of the button.
     */
    private val buttonHeight: Int
        get() = when {
            iconOnly -> ICON_ONLY
            size == SidebarMenuButtonSize.SM -> 14
            size == SidebarMenuButtonSize.LG -> 24
            else -> 16
        }

    /**
     * Returns the size of the icon and text with padding, or the square while only the icon is
     * drawn.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        if (iconOnly) return Size(ICON_ONLY, ICON_ONLY)
        return Size(PADDING + (if (icon != null) ICON + GAP else 0) + measurer.width(text) + PADDING + reservedEnd, buttonHeight)
    }

    /**
     * Keeps the button square while only the icon is drawn.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox =
        if (iconOnly) LayoutBox(width = Sizing.fixed(ICON_ONLY), height = Sizing.fixed(ICON_ONLY), content = Size(ICON_ONLY, ICON_ONLY)).also { layoutBox = it }
        else super.createLayout(measurer)

    /**
     * Draws the background, the icon and the text, and asks for the tooltip while only the icon
     * is drawn and the mouse is on the button.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val hovered = enabled && isOver(mouseX, mouseY)
        if (variant == SidebarMenuButtonVariant.OUTLINE) {
            ui.fillRounded(bounds, if (hovered) ui.tokens.accent else ui.tokens.background)
            ui.borderRounded(bounds, if (hovered) ui.tokens.accent else ui.tokens.border)
        } else if (hovered || active) {
            ui.fillRounded(bounds, ui.tokens.accent)
        }
        val base = if (hovered || active) ui.tokens.accentForeground else ui.tokens.foreground
        val color = if (enabled) base else ui.disabled(base)
        if (iconOnly) {
            icon?.let { ui.icon(it, Rect(bounds.x + (bounds.width - ICON) / 2, bounds.y + (bounds.height - ICON) / 2, ICON, ICON), color) }
            if (wantsTooltip && hovered) context.showTooltip(tooltip, bounds, tooltipSide)
            return
        }
        var x = bounds.x + PADDING
        icon?.let {
            ui.icon(it, Rect(x, bounds.y + (bounds.height - ICON) / 2, ICON, ICON), color)
            x += ICON + GAP
        }
        ui.clipped(Rect(bounds.x, bounds.y, (bounds.width - reservedEnd - PADDING).coerceAtLeast(0), bounds.height)) {
            ui.text(text, x, bounds.y + (bounds.height - ui.lineHeight + 1) / 2, color)
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
     * Holds the button metrics.
     */
    companion object {
        /**
         * The width and height of the button while only its icon is drawn.
         */
        const val ICON_ONLY: Int = 16

        /**
         * The space around the content.
         */
        const val PADDING: Int = 4

        /**
         * The size of the icon.
         */
        const val ICON: Int = 8

        /**
         * The space between the icon and the text.
         */
        const val GAP: Int = 4
    }
}

/**
 * A small count or label at the end of a sidebar menu button, in muted text.
 *
 * @param id the id of the widget
 * @param text the text as component JSON
 */
class SidebarMenuBadgeWidget(id: String, var text: String) : Widget(id) {

    /**
     * Returns the size of the text with padding, at least a square.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(maxOf(HEIGHT, measurer.width(text) + 2 * PADDING_X), HEIGHT)

    /**
     * Draws the text centered.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.centeredText(text, bounds, ui.tokens.mutedForeground)
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
     * Holds the badge metrics.
     */
    private companion object {
        /**
         * The height of the badge.
         */
        const val HEIGHT: Int = 10

        /**
         * The space left and right of the text.
         */
        const val PADDING_X: Int = 2
    }
}

/**
 * A placeholder for a sidebar menu button that is still loading: an optional square for the icon
 * and a bar of a width between half and nine tenths of the button, fixed per id.
 *
 * @param id the id of the widget
 * @property showIcon whether a placeholder for the icon is drawn
 */
class SidebarMenuSkeletonWidget(id: String, val showIcon: Boolean) : Widget(id) {

    /**
     * Returns the height of a menu button.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(HEIGHT, HEIGHT)

    /**
     * Draws the placeholders in the muted colour.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        var x = bounds.x + PADDING
        val bar = 8
        if (showIcon) {
            ui.fillRounded(Rect(x, bounds.y + (bounds.height - bar) / 2, bar, bar), ui.tokens.muted, 2)
            x += bar + PADDING
        }
        val share = 50 + abs(id.hashCode() % 41)
        val width = ((bounds.right - PADDING - x) * share / 100).coerceAtLeast(0)
        ui.fillRounded(Rect(x, bounds.y + (bounds.height - bar) / 2, width, bar), ui.tokens.muted, 2)
    }

    /**
     * Holds the skeleton metrics.
     */
    private companion object {
        /**
         * The height of the skeleton.
         */
        const val HEIGHT: Int = 16

        /**
         * The space before and between the placeholders.
         */
        const val PADDING: Int = 4
    }
}

/**
 * A sub-menu below a sidebar menu button: its items, indented with a line at the start.
 *
 * @param id the id of the widget
 */
class SidebarMenuSubWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
        gap = 2
        padding = Insets(1, 4, 1, LINE_OFFSET + 5)
    }

    /**
     * Draws the line at the start, then the items.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.fill(Rect(bounds.x + LINE_OFFSET, bounds.y, 1, bounds.height), ui.tokens.border)
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Holds the sub-menu metrics.
     */
    private companion object {
        /**
         * How far the line is indented from the start of the sub-menu.
         */
        const val LINE_OFFSET: Int = 7
    }
}

/**
 * The button of a sidebar sub-menu item: its icon and text, highlighted while hovered or
 * active.
 *
 * @param id the id of the widget
 * @param text the text as component JSON
 * @property icon the Lucide name of the icon, or `null` for none
 * @property size the size of the text
 * @property active whether the button leads to the current page
 */
class SidebarMenuSubButtonWidget(id: String, var text: String, val icon: String?, val size: SidebarMenuSubButtonSize, val active: Boolean) : ClickableWidget(id) {

    /**
     * Returns the size of the icon and text with padding.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        Size(PADDING + (if (icon != null) ICON + GAP else 0) + measurer.width(text) + PADDING, HEIGHT)

    /**
     * Draws the background while hovered or active, the icon and the text.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val hovered = enabled && isOver(mouseX, mouseY)
        if (hovered || active) ui.fillRounded(bounds, ui.tokens.accent)
        val base = if (hovered || active) ui.tokens.accentForeground else ui.tokens.foreground
        val color = if (enabled) base else ui.disabled(base)
        var x = bounds.x + PADDING
        icon?.let {
            ui.icon(it, Rect(x, bounds.y + (bounds.height - ICON) / 2, ICON, ICON), color)
            x += ICON + GAP
        }
        ui.clipped(bounds) {
            if (size == SidebarMenuSubButtonSize.SM) ui.wrappedText(text, x, bounds.y + (bounds.height - ui.lineHeight) / 2 + 1, bounds.right - x, color, maxLines = 1, scale = SMALL)
            else ui.text(text, x, bounds.y + (bounds.height - ui.lineHeight + 1) / 2, color)
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
     * Holds the button metrics.
     */
    private companion object {
        /**
         * The height of the button.
         */
        const val HEIGHT: Int = 14

        /**
         * The space left and right of the content.
         */
        const val PADDING: Int = 4

        /**
         * The size of the icon.
         */
        const val ICON: Int = 8

        /**
         * The space between the icon and the text.
         */
        const val GAP: Int = 4

        /**
         * The scale of small text.
         */
        const val SMALL: Float = 0.8f
    }
}

/**
 * A small ghost button with a panel icon that expands and collapses the sidebar of its
 * provider.
 *
 * @param id the id of the widget
 */
class SidebarTriggerWidget(id: String) : SidebarIconButtonWidget(id, "panel-left") {

    /**
     * Returns the size of the button.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(TRIGGER, TRIGGER)

    /**
     * Holds the trigger metrics.
     */
    private companion object {
        /**
         * The width and height of the trigger.
         */
        const val TRIGGER: Int = 14
    }
}

/**
 * A thin strip along the inner edge of a sidebar that expands and collapses it when clicked. It
 * shows a line while hovered and does not take the focus.
 *
 * @param id the id of the widget
 */
class SidebarRailWidget(id: String) : ClickableWidget(id) {

    /**
     * The rail cannot take the focus.
     */
    override val focusable: Boolean get() = false

    /**
     * Returns no size; the sidebar places the rail.
     *
     * @param measurer the text measurer
     * @return zero
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size.ZERO

    /**
     * Fires the rail on a left click without taking the focus.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on the rail
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) context.actionTriggered(this, submitsInput = false)
        return true
    }

    /**
     * Draws a line in the middle of the strip while hovered.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        if (isOver(mouseX, mouseY)) ui.fill(Rect(bounds.x + bounds.width / 2, bounds.y, 1, bounds.height), ThemeColors.withAlpha(ui.tokens.ring, HOVER_ALPHA))
    }

    /**
     * Holds the rail metrics.
     */
    companion object {
        /**
         * Half the width of the strip.
         */
        const val HALF: Int = 2

        /**
         * The opacity of the line while hovered.
         */
        private const val HOVER_ALPHA: Float = 0.8f
    }
}

package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.Corners
import dev.slne.surf.roleplay.fabric.ui.OverlayPlacement
import dev.slne.surf.roleplay.fabric.ui.TextAlign
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.AlertDialogSize
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import dev.slne.surf.roleplay.protocol.screen.Sizing
import org.lwjgl.glfw.GLFW

/**
 * How a modal overlay is placed in the window.
 */
enum class ModalKind {
    /**
     * Centered, closed by a click outside.
     */
    DIALOG,

    /**
     * Centered, not closed by a click outside.
     */
    ALERT_DIALOG,

    /**
     * Attached to an edge of the window.
     */
    SHEET,

    /**
     * Attached to an edge of the window, and at most four fifths of its height when at the top or
     * bottom.
     */
    DRAWER,
}

/**
 * A modal overlay: a dialog, alert dialog, sheet or drawer. It dims everything below it, keeps
 * the focus inside its content, and closes after an action inside one of its close parts.
 *
 * @param owner the host that opened the overlay
 * @property kind how the overlay is placed
 * @property edge the window edge a sheet or drawer is attached to
 */
class ModalPopover(owner: OverlayHostWidget, val kind: ModalKind, val edge: OverlaySide) :
    WidgetPopover(owner, OverlaySide.BOTTOM, Align.CENTER, modal = true, dismissOnOutsideClick = kind != ModalKind.ALERT_DIALOG, trapFocus = true) {

    /**
     * Lays the content out in its place inside an area and returns the place.
     *
     * @param window the area the overlay is confined to: its overlay container or the panel
     *        content
     * @param measurer the text measurer
     * @return the content's area
     */
    override fun area(window: Rect, measurer: TextMeasurer): Rect {
        val shown = content ?: return Rect.EMPTY
        val box = shown.createLayout(measurer)
        val rect = when (kind) {
            ModalKind.DIALOG, ModalKind.ALERT_DIALOG -> {
                val width = FlexLayout.measure(box, window.width - 2 * MARGIN).width.coerceAtMost(window.width - 2 * MARGIN)
                val height = FlexLayout.measureAt(box, width).height.coerceAtMost(window.height - 2 * MARGIN)
                Rect(window.x + (window.width - width) / 2, window.y + (window.height - height) / 2, width, height)
            }
            ModalKind.SHEET, ModalKind.DRAWER -> edgeArea(box, window)
        }
        FlexLayout.layout(box, rect)
        shown.applyLayout()
        return rect
    }

    /**
     * Computes the area of a sheet or drawer at its edge.
     *
     * @param box the layout box of the content
     * @param window the window area
     * @return the area
     */
    private fun edgeArea(box: LayoutBox, window: Rect): Rect = when (edge) {
        OverlaySide.LEFT, OverlaySide.RIGHT -> {
            val width = minOf(window.width * 3 / 4, SheetContentWidget.SIDE_WIDTH)
            Rect(if (edge == OverlaySide.RIGHT) window.right - width else window.x, window.y, width, window.height)
        }
        OverlaySide.TOP, OverlaySide.BOTTOM -> {
            val limit = if (kind == ModalKind.DRAWER) window.height * 4 / 5 else window.height
            val height = FlexLayout.measureAt(box, window.width).height.coerceAtMost(limit)
            Rect(window.x, if (edge == OverlaySide.BOTTOM) window.bottom - height else window.y, window.width, height)
        }
    }

    /**
     * Closes the overlay after an action of a widget inside one of its close parts.
     *
     * @param context the screen showing the overlay
     * @param widget the widget
     */
    override fun afterAction(context: UiContext, widget: Widget) {
        val root = content ?: return
        var current = widget
        while (true) {
            val parent = WidgetTree.parentOf(root, current.id) ?: return
            if (parent is DialogCloseWidget) {
                context.closePopover(this)
                return
            }
            current = parent
        }
    }

    /**
     * Holds the margin of centered overlays.
     */
    private companion object {
        /**
         * The space kept between a centered overlay and the window edges.
         */
        const val MARGIN: Int = OverlayPlacement.WINDOW_MARGIN * 4
    }
}

/**
 * A host of a modal overlay: its triggers open the overlay's content.
 *
 * @param id the id of the widget
 * @property kind how the overlay is placed
 */
class ModalHostWidget(id: String, val kind: ModalKind) : OverlayHostWidget(id) {

    /**
     * Creates the modal overlay, attached to the edge its content asks for.
     *
     * @return the overlay
     */
    override fun createPopover(): Popover {
        val edge = when (val shown = content) {
            is SheetContentWidget -> shown.side
            is DrawerContentWidget -> shown.direction
            else -> OverlaySide.BOTTOM
        }
        (content as? ModalSurfaceWidget)?.host = this
        return ModalPopover(this, kind, edge)
    }
}

/**
 * The content of a modal overlay with an optional close button at its top right.
 *
 * @param id the id of the widget
 * @property showCloseButton whether the close button is drawn
 */
abstract class ModalSurfaceWidget(id: String, val showCloseButton: Boolean) : OverlayContentWidget(id) {

    /**
     * The host whose overlay shows this content, linked when the overlay opens.
     */
    var host: OverlayHostWidget? = null

    /**
     * Returns the area of the close button.
     *
     * @return the area
     */
    fun closeButton(): Rect = Rect(bounds.right - CLOSE_INSET - CLOSE_SIZE, bounds.y + CLOSE_INSET, CLOSE_SIZE, CLOSE_SIZE)

    /**
     * Draws the close button, brighter under the mouse.
     *
     * @param ui the graphics to draw with
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    protected fun drawCloseButton(ui: UiGraphics, mouseX: Int, mouseY: Int) {
        if (!showCloseButton) return
        val area = closeButton()
        val hovered = area.grow(2).contains(mouseX.toDouble(), mouseY.toDouble())
        ui.icon("x", area, ThemeColors.withAlpha(ui.tokens.foreground, if (hovered) 1f else CLOSE_ALPHA))
    }

    /**
     * Closes the overlay on a click on the close button, and passes other clicks to the content.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was handled
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (showCloseButton && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && closeButton().grow(2).contains(x, y)) {
            host?.close(context)
            return true
        }
        return super.mouseClicked(context, x, y, button)
    }

    /**
     * Holds the close button metrics.
     */
    companion object {
        /**
         * The side of the close button.
         */
        const val CLOSE_SIZE: Int = 8

        /**
         * The space between the close button and the content edges.
         */
        const val CLOSE_INSET: Int = 8

        /**
         * The opacity of the close button while the mouse is not on it.
         */
        private const val CLOSE_ALPHA: Float = 0.7f
    }
}

/**
 * The content of a dialog: a padded stack on a bordered surface in the background colour.
 *
 * @param id the id of the widget
 * @param showCloseButton whether a close button is drawn at the top right
 */
open class DialogContentWidget(id: String, showCloseButton: Boolean) : ModalSurfaceWidget(id, showCloseButton) {
    init {
        padding = Insets(PADDING, PADDING, PADDING, PADDING)
        gap = GAP
    }

    /**
     * Draws the surface, the content and the close button.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        ui.fillRounded(bounds, tokens.background)
        ui.borderRounded(bounds, tokens.border)
        super.render(ui, context, mouseX, mouseY)
        drawCloseButton(ui, mouseX, mouseY)
    }

    /**
     * Holds the dialog spacing.
     */
    companion object {
        /**
         * The space inside the surface.
         */
        const val PADDING: Int = 12

        /**
         * The space between two parts of the content.
         */
        const val GAP: Int = 8
    }
}

/**
 * The content of an alert dialog. The small size centers its header and places its footer
 * buttons side by side at equal widths.
 *
 * @param id the id of the widget
 * @property size the size of the dialog
 */
class AlertDialogContentWidget(id: String, val size: AlertDialogSize) : DialogContentWidget(id, false) {

    /**
     * Applies the size to the header and footer, then creates the layout box.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        val small = size == AlertDialogSize.SM
        childList.forEach { child ->
            when (child) {
                is DialogHeaderWidget -> child.centered = small
                is DialogFooterWidget -> child.even = small
            }
        }
        return super.createLayout(measurer)
    }
}

/**
 * The header of a dialog: its media, title and description, stacked, optionally centered.
 *
 * @param id the id of the widget
 */
class DialogHeaderWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = 4
        crossAlign = Align.STRETCH
    }

    /**
     * Whether the texts and media are centered.
     */
    var centered: Boolean = false

    /**
     * Centers or aligns the texts and media, then creates the layout box.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        childList.forEach { child ->
            when (child) {
                is TextWidget -> child.alignOverride = if (centered) TextAlign.CENTER else null
                is AlertDialogMediaWidget -> child.centered = centered
            }
        }
        return super.createLayout(measurer)
    }
}

/**
 * The footer of a dialog: its buttons in a row at the end, or side by side at equal widths.
 *
 * @param id the id of the widget
 */
class DialogFooterWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = 4
        mainAlign = Align.END
        crossAlign = Align.CENTER
    }

    /**
     * Whether the buttons share the width equally.
     */
    var even: Boolean = false

    /**
     * Shares the width among the buttons when asked, then creates the layout box.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        if (even) {
            childList.forEach { child ->
                child.width = Sizing.grow()
                if (child is DialogCloseWidget) child.childList.forEach { it.width = Sizing.grow() }
            }
        }
        return super.createLayout(measurer)
    }
}

/**
 * A part of an overlay whose actions close the overlay after they fire.
 *
 * @param id the id of the widget
 */
class DialogCloseWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = 4
        crossAlign = Align.CENTER
    }
}

/**
 * An icon on a muted square above the title of an alert dialog.
 *
 * @param id the id of the widget
 * @property icon the Lucide name of the icon
 */
class AlertDialogMediaWidget(id: String, val icon: String) : Widget(id) {

    /**
     * Whether the square is centered in the widget's width.
     */
    var centered: Boolean = false

    /**
     * Returns the square with the space below it.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(BOX, BOX + MARGIN)

    /**
     * Draws the square and the icon.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val x = if (centered) bounds.x + (bounds.width - BOX) / 2 else bounds.x
        val box = Rect(x, bounds.y, BOX, BOX)
        ui.fillRounded(box, ui.tokens.muted)
        ui.icon(icon, Rect(box.x + (BOX - ICON) / 2, box.y + (BOX - ICON) / 2, ICON, ICON), ui.tokens.foreground)
    }

    /**
     * Holds the media metrics.
     */
    private companion object {
        /**
         * The side of the square.
         */
        const val BOX: Int = 32

        /**
         * The side of the icon.
         */
        const val ICON: Int = 16

        /**
         * The space below the square.
         */
        const val MARGIN: Int = 4
    }
}

/**
 * The content of a sheet: a stack in the background colour attached to an edge, with a border on
 * its inner edge, its footer at the far end and an optional close button.
 *
 * @param id the id of the widget
 * @property side the edge of the window the sheet is attached to
 * @param showCloseButton whether a close button is drawn at the top right
 */
open class SheetContentWidget(id: String, val side: OverlaySide, showCloseButton: Boolean) : ModalSurfaceWidget(id, showCloseButton) {
    init {
        gap = GAP
    }

    /**
     * Creates a stack of the parts with a growing space before the footer.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        val boxes = mutableListOf<LayoutBox>()
        shownChildren.forEach { child ->
            if (child is SheetFooterWidget) boxes += LayoutBox(height = Sizing.grow())
            boxes += child.createLayout(measurer)
        }
        return LayoutBox(width = width, height = height, axis = Axis.VERTICAL, children = boxes, gap = gap, padding = padding, crossAlign = Align.STRETCH).also { layoutBox = it }
    }

    /**
     * Draws the surface, the inner border, the content and the close button.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        drawSurface(ui)
        super.render(ui, context, mouseX, mouseY)
        drawCloseButton(ui, mouseX, mouseY)
    }

    /**
     * Draws the surface and the border on the inner edge.
     *
     * @param ui the graphics to draw with
     */
    protected open fun drawSurface(ui: UiGraphics) {
        val tokens = ui.tokens
        ui.fill(bounds, tokens.background)
        val border = when (side) {
            OverlaySide.RIGHT -> Rect(bounds.x, bounds.y, 1, bounds.height)
            OverlaySide.LEFT -> Rect(bounds.right - 1, bounds.y, 1, bounds.height)
            OverlaySide.TOP -> Rect(bounds.x, bounds.bottom - 1, bounds.width, 1)
            OverlaySide.BOTTOM -> Rect(bounds.x, bounds.y, bounds.width, 1)
        }
        ui.fill(border, tokens.border)
    }

    /**
     * Holds the sheet metrics.
     */
    companion object {
        /**
         * The width of a sheet at the left or right edge, unless the window is narrow.
         */
        const val SIDE_WIDTH: Int = 192

        /**
         * The space between two parts of the content.
         */
        const val GAP: Int = 8
    }
}

/**
 * The content of a drawer: like a sheet without a close button, with rounded corners on its inner
 * edge and a handle when it comes from the bottom.
 *
 * @param id the id of the widget
 * @property direction the edge of the window the drawer comes in from
 */
class DrawerContentWidget(id: String, val direction: OverlaySide) : SheetContentWidget(id, direction, false) {
    init {
        if (direction == OverlaySide.BOTTOM) padding = Insets(top = HANDLE_SPACE)
    }

    /**
     * Centers the header texts of drawers at the top or bottom, then creates the layout box.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        val centered = direction == OverlaySide.BOTTOM || direction == OverlaySide.TOP
        childList.filterIsInstance<SheetHeaderWidget>().forEach { it.centered = centered }
        return super.createLayout(measurer)
    }

    /**
     * Draws the surface with rounded inner corners, the border and the handle.
     *
     * @param ui the graphics to draw with
     */
    override fun drawSurface(ui: UiGraphics) {
        val tokens = ui.tokens
        val corners = when (direction) {
            OverlaySide.BOTTOM -> Corners(topLeft = true, topRight = true, bottomLeft = false, bottomRight = false)
            OverlaySide.TOP -> Corners(topLeft = false, topRight = false, bottomLeft = true, bottomRight = true)
            OverlaySide.RIGHT -> Corners(topLeft = true, topRight = false, bottomLeft = true, bottomRight = false)
            OverlaySide.LEFT -> Corners(topLeft = false, topRight = true, bottomLeft = false, bottomRight = true)
        }
        ui.fillRounded(bounds, tokens.background, corners = corners)
        ui.borderRounded(bounds, tokens.border, corners = corners)
        if (direction == OverlaySide.BOTTOM) {
            ui.fillRounded(Rect(bounds.x + (bounds.width - HANDLE_WIDTH) / 2, bounds.y + HANDLE_TOP, HANDLE_WIDTH, HANDLE_HEIGHT), tokens.muted, HANDLE_HEIGHT / 2)
        }
    }

    /**
     * Holds the handle metrics.
     */
    private companion object {
        /**
         * The width of the handle.
         */
        const val HANDLE_WIDTH: Int = 50

        /**
         * The height of the handle.
         */
        const val HANDLE_HEIGHT: Int = 4

        /**
         * The space above the handle.
         */
        const val HANDLE_TOP: Int = 8

        /**
         * The space the handle takes above the content.
         */
        const val HANDLE_SPACE: Int = 16
    }
}

/**
 * The header of a sheet or drawer: its title and description, stacked, optionally centered.
 *
 * @param id the id of the widget
 */
class SheetHeaderWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = 3
        padding = Insets(SheetFooterWidget.PADDING, SheetFooterWidget.PADDING, SheetFooterWidget.PADDING, SheetFooterWidget.PADDING)
        crossAlign = Align.STRETCH
    }

    /**
     * Whether the texts are centered.
     */
    var centered: Boolean = false

    /**
     * Centers or aligns the texts, then creates the layout box.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        childList.filterIsInstance<TextWidget>().forEach { it.alignOverride = if (centered) TextAlign.CENTER else null }
        return super.createLayout(measurer)
    }
}

/**
 * The footer of a sheet or drawer: its buttons stacked at the end of the content.
 *
 * @param id the id of the widget
 */
class SheetFooterWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = 4
        padding = Insets(PADDING, PADDING, PADDING, PADDING)
        crossAlign = Align.STRETCH
    }

    /**
     * Holds the footer padding.
     */
    companion object {
        /**
         * The space inside the header and footer of sheets and drawers.
         */
        const val PADDING: Int = 8
    }
}

/**
 * A region that confines the modal overlays opened inside it: their content is placed inside the
 * region and their backdrop dims only the region. Its children are stacked across its width.
 *
 * @param id the id of the widget
 */
class OverlayContainerWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }
}

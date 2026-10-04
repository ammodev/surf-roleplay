package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.screen.ScreenRules
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.text.ScreenText
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeTokens
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.fabric.ui.widget.ButtonWidget
import dev.slne.surf.roleplay.fabric.ui.widget.FormWidget
import dev.slne.surf.roleplay.fabric.ui.widget.ActionInterceptor
import dev.slne.surf.roleplay.fabric.ui.widget.KeyInterceptor
import dev.slne.surf.roleplay.fabric.ui.widget.ShortcutWidget
import dev.slne.surf.roleplay.fabric.ui.widget.OverlayContainerWidget
import dev.slne.surf.roleplay.fabric.ui.widget.OverlayHostWidget
import dev.slne.surf.roleplay.fabric.ui.widget.Popover
import dev.slne.surf.roleplay.fabric.ui.widget.ScrollContainer
import dev.slne.surf.roleplay.fabric.ui.widget.UiContext
import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import dev.slne.surf.roleplay.protocol.screen.Presentation
import dev.slne.surf.roleplay.protocol.screen.SheetEdge
import dev.slne.surf.roleplay.protocol.screen.SizeMode
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

/**
 * Receives what the player does on a [ScreenPanel].
 */
interface ScreenPanelListener {
    /**
     * Called when the player triggers a widget action, such as clicking a button or pressing a
     * toggle.
     *
     * @param panel the panel
     * @param widget the widget
     */
    fun actionTriggered(panel: ScreenPanel, widget: Widget)

    /**
     * Called when the player closes a closable panel with Escape or a click on the backdrop.
     *
     * @param panel the panel
     */
    fun closeRequested(panel: ScreenPanel)

    /**
     * Called when an input that reports its changes changed its value.
     *
     * @param panel the panel
     * @param widget the input
     */
    fun valueChanged(panel: ScreenPanel, widget: Widget) = Unit

    /**
     * Called when a combobox that reports its searches changed its typed query.
     *
     * @param panel the panel
     * @param widget the combobox
     * @param query the query
     */
    fun searchChanged(panel: ScreenPanel, widget: Widget, query: String) = Unit
}

/**
 * How a panel is drawn: its theme and its position relative to the window.
 *
 * @property tokens the design tokens the panel is drawn with
 * @property presentation how the panel is shown relative to the panels below it
 * @property sheetEdge the window edge a sheet is attached to
 */
data class PanelStyle(
    val tokens: ThemeTokens,
    val presentation: Presentation = Presentation.SCREEN,
    val sheetEdge: SheetEdge = SheetEdge.RIGHT,
)

/**
 * A titled panel that shows a widget tree, as one layer of a [RoleplayScreen].
 *
 * The panel is centered in the window, or attached to an edge for sheets, which slide in when
 * the panel opens. Content taller than the window scrolls inside the panel. The panel handles the
 * keyboard focus: Tab and Shift+Tab move it through the tree's focusable widgets, the focused
 * widget shows a focus ring and is scrolled into view.
 *
 * @param titleJson the title as component JSON
 * @param root the root of the widget tree
 * @property closable whether Escape or a click on the backdrop closes the panel
 * @property listener the receiver of button activations and close requests
 * @property style how the panel is drawn
 */
class ScreenPanel(
    val titleJson: String,
    root: Widget,
    val closable: Boolean,
    private val listener: ScreenPanelListener,
    val style: PanelStyle,
) : UiContext {

    /**
     * The design tokens the panel is drawn with.
     */
    val tokens: ThemeTokens get() = style.tokens

    /**
     * The root of the widget tree. Replacing it drops the focus and lays the panel out again.
     */
    var root: Widget = root
        set(value) {
            field = value
            focusedWidget = null
            stack.clear()
            requestLayout()
        }

    /**
     * The widget that receives keyboard input, or `null` if none does.
     */
    override var focusedWidget: Widget? = null
        private set

    /**
     * The open overlays, from bottom to top.
     */
    private val stack = mutableListOf<Popover>()

    /**
     * The top open overlay, or `null` if none is open.
     */
    override val popover: Popover? get() = stack.lastOrNull()

    /**
     * The open overlays, from bottom to top.
     */
    override val popovers: List<Popover> get() = stack.toList()

    /**
     * The tooltip asked for during the current frame, or `null` for none.
     */
    private var tooltip: TooltipRequest? = null

    /**
     * The drawing code asked to run above everything during the current frame, or `null`.
     */
    private var onTop: ((UiGraphics) -> Unit)? = null

    /**
     * Whether the focus ring is shown: after the player used the keyboard, and not after a mouse
     * click.
     */
    override var focusVisible: Boolean = false
        private set

    /**
     * The widget that receives mouse movement while the button is held, or `null` if none does.
     */
    private var dragTarget: Widget? = null

    /**
     * The widget that asked to be dragged during the current click, or `null` if none did.
     */
    private var requestedDrag: Widget? = null

    /**
     * Whether the tree must be laid out before the next frame.
     */
    private var layoutPending: Boolean = true

    /**
     * Whether the focused widget must be scrolled into view at the next layout.
     */
    private var revealPending: Boolean = false

    /**
     * The vertical scroll position of the content.
     */
    private val scroll = PanelScroll()

    /**
     * The text inputs whose changes wait for the player to pause typing.
     */
    private val debouncer = ChangeDebouncer(CHANGE_DELAY_MILLIS)

    /**
     * The comboboxes whose queries wait for the player to pause typing.
     */
    private val searches = ChangeDebouncer(CHANGE_DELAY_MILLIS)

    /**
     * The latest typed query of every combobox that waits in [searches].
     */
    private val pendingQueries = mutableMapOf<String, String>()

    /**
     * The measurer of the font the panel was last laid out with.
     */
    private var measurer: TextMeasurer? = null

    /**
     * The area of the panel including its title bar, set by the last layout.
     */
    var panel: Rect = Rect.EMPTY
        private set

    /**
     * The area in which the content is shown, set by the last layout.
     */
    private var viewport: Rect = Rect.EMPTY

    /**
     * The area in which the content is shown, set by the last layout.
     */
    internal val contentArea: Rect get() = viewport

    /**
     * The window size of the last layout.
     */
    private var window: Rect = Rect.EMPTY

    /**
     * When the panel was created, for the sheet slide-in.
     */
    val openedAt: Long = System.currentTimeMillis()

    /**
     * Whether the sheet was still sliding in at the last layout check.
     */
    private var wasSliding: Boolean = true

    /**
     * Whether Shift was held when the last mouse click was pressed.
     */
    override var shiftClick: Boolean = false
        private set

    /**
     * The text on the system clipboard.
     */
    override var clipboard: String
        get() = Minecraft.getInstance().keyboardHandler.clipboard
        set(value) {
            Minecraft.getInstance().keyboardHandler.clipboard = value
        }

    /**
     * Lays the panel out for a window size if the size changed or a layout was requested.
     *
     * @param font the font texts are measured with
     * @param width the window width in GUI pixels
     * @param height the window height in GUI pixels
     */
    fun layoutIfNeeded(font: Font, width: Int, height: Int) = layoutIfNeeded(FontTextMeasurer(font), width, height)

    /**
     * Lays the panel out for a window size if the size changed or a layout was requested,
     * measuring texts with a measurer.
     *
     * @param measurer the text measurer
     * @param width the window width in GUI pixels
     * @param height the window height in GUI pixels
     */
    internal fun layoutIfNeeded(measurer: TextMeasurer, width: Int, height: Int) {
        val slidingNow = sliding()
        val slideEnded = wasSliding && !slidingNow
        wasSliding = slidingNow
        if (!layoutPending && window.width == width && window.height == height && !slidingNow && !slideEnded) return
        window = Rect(0, 0, width, height)
        layout(measurer)
        if (revealPending) {
            revealPending = false
            if (revealInLists()) layout(measurer)
            if (revealInPanel()) layout(measurer)
        }
    }

    /**
     * Lays the tree out at its full height and places the panel and the viewport for the
     * presentation, capping the panel at the window and scrolling the rest. A root that grows in
     * height and holds growing scroll areas is laid out at the window height instead, so that the
     * areas scroll.
     *
     * @param measurer the text measurer
     */
    private fun layout(measurer: TextMeasurer) {
        layoutPending = false
        this.measurer = measurer
        WidgetTree.resolveDirection(root)
        val box = root.createLayout(measurer)
        val chromeX = 2 * UiMetrics.PANEL_PADDING
        val chromeY = 2 * UiMetrics.PANEL_PADDING + UiMetrics.TITLE_BAR_HEIGHT
        val sheet = style.presentation == Presentation.SHEET
        val margin = if (sheet) 0 else UiMetrics.SCREEN_MARGIN
        val maxWidth = (window.width - 2 * margin - chromeX).coerceAtLeast(0)
        val maxHeight = (window.height - 2 * margin - chromeY).coerceAtLeast(0)
        val verticalSheet = sheet && (style.sheetEdge == SheetEdge.LEFT || style.sheetEdge == SheetEdge.RIGHT)
        val horizontalSheet = sheet && !verticalSheet

        val titleWidth = measurer.width(titleJson) + chromeX
        val contentWidth = when {
            horizontalSheet || root.width.mode == SizeMode.GROW -> maxWidth
            else -> FlexLayout.measure(box, maxWidth).width.coerceAtMost(maxWidth)
        }
        val preferredHeight = FlexLayout.measureAt(box, contentWidth).height
        val shortestHeight = if (root.height.mode == SizeMode.GROW) FlexLayout.shortestAt(box, contentWidth) else preferredHeight
        val contentHeight = PanelSizing.contentHeight(root.height.mode, preferredHeight, shortestHeight, maxHeight)
        val viewportHeight = if (verticalSheet) maxHeight else contentHeight.coerceAtMost(maxHeight)
        scroll.update(contentHeight, viewportHeight)

        val panelWidth = if (horizontalSheet) window.width else maxOf(contentWidth + chromeX, titleWidth).coerceAtMost(window.width - 2 * margin)
        val panelHeight = viewportHeight + chromeY
        val (x, y) = when {
            !sheet -> (window.width - panelWidth) / 2 to (window.height - panelHeight) / 2
            style.sheetEdge == SheetEdge.RIGHT -> window.width - panelWidth to 0
            style.sheetEdge == SheetEdge.LEFT -> 0 to 0
            style.sheetEdge == SheetEdge.TOP -> 0 to 0
            else -> 0 to window.height - panelHeight
        }
        panel = slide(Rect(x, y, panelWidth, panelHeight))
        viewport = Rect(
            panel.x + UiMetrics.PANEL_PADDING,
            panel.y + UiMetrics.TITLE_BAR_HEIGHT + UiMetrics.PANEL_PADDING,
            panelWidth - chromeX,
            viewportHeight,
        )
        val contentX = viewport.x + (viewport.width - contentWidth) / 2
        FlexLayout.layout(box, Rect(contentX, viewport.y - scroll.offset, contentWidth, contentHeight))
        root.applyLayout()
        WidgetTree.visit(root) { if (it is OverlayHostWidget) it.sync(this) }
    }

    /**
     * Lays out every open overlay and returns their areas, from bottom to top.
     *
     * @return the areas
     */
    internal fun overlayAreas(): List<Rect> = stack.map { popoverArea(it) }

    /**
     * Returns the index of the overlay the mouse is on: the topmost one whose area contains the
     * mouse, unless a modal overlay above it covers it.
     *
     * @param areas the areas of the open overlays, bottom first
     * @param x the mouse x position
     * @param y the mouse y position
     * @return the index, or -1 if the mouse is on no overlay it can reach
     */
    internal fun mouseOverlay(areas: List<Rect>, x: Int, y: Int): Int {
        val hit = areas.indexOfLast { it.contains(x.toDouble(), y.toDouble()) }
        return if (hit < stack.indexOfLast { it.modal }) -1 else hit
    }

    /**
     * Returns the widget to scroll into view: the focused widget, or for a widget inside an
     * overlay the host of the lowest overlay, or else the owner of the open overlay.
     *
     * @return the widget, or `null` for none
     */
    private fun revealTarget(): Widget? {
        val focused = focusedWidget ?: return popover?.owner
        return stack.firstOrNull { it.containsWidget(focused) }?.let { stack.first().owner } ?: focused
    }

    /**
     * Moves a sheet's panel out towards its edge while it slides in.
     *
     * @param target the panel's final area
     * @return the panel's current area
     */
    private fun slide(target: Rect): Rect {
        if (style.presentation != Presentation.SHEET) return target
        val progress = ((System.currentTimeMillis() - openedAt) / SLIDE_MILLIS.toFloat()).coerceIn(0f, 1f)
        val remaining = (1f - progress) * (1f - progress)
        val dx = (remaining * target.width).roundToInt()
        val dy = (remaining * target.height).roundToInt()
        return when (style.sheetEdge) {
            SheetEdge.RIGHT -> target.copy(x = target.x + dx)
            SheetEdge.LEFT -> target.copy(x = target.x - dx)
            SheetEdge.TOP -> target.copy(y = target.y - dy)
            SheetEdge.BOTTOM -> target.copy(y = target.y + dy)
        }
    }

    /**
     * Returns whether a sheet is still sliding in.
     *
     * @return whether the panel moves this frame
     */
    private fun sliding(): Boolean =
        style.presentation == Presentation.SHEET && System.currentTimeMillis() - openedAt < SLIDE_MILLIS

    /**
     * Scrolls the scroll lists and scroll areas around the focused widget, innermost first, so
     * that the widget is visible inside them.
     *
     * @return whether a list scrolled, in which case the panel must be laid out again
     */
    private fun revealInLists(): Boolean {
        val focused = revealTarget() ?: return false
        var moved = false
        var current: Widget = focused
        while (true) {
            val parent = WidgetTree.parentOf(root, current.id) ?: break
            if (parent is ScrollContainer) moved = parent.ensureVisible(focused) || moved
            current = parent
        }
        return moved
    }

    /**
     * Scrolls the panel so that the focused widget is visible, using the bounds of the last layout.
     *
     * @return whether the panel scrolled, in which case it must be laid out again
     */
    private fun revealInPanel(): Boolean {
        val focused = revealTarget() ?: return false
        val before = scroll.offset
        val top = focused.bounds.y - (viewport.y - scroll.offset)
        scroll.ensureVisible(top - FOCUS_MARGIN, top + focused.bounds.height + FOCUS_MARGIN)
        return scroll.offset != before
    }

    /**
     * Draws the panel, its content, the scroll bar, the focus ring and any open popover.
     *
     * @param graphics the GUI graphics of this frame
     * @param font the font texts are drawn with
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param active whether this panel receives input; inactive panels ignore the mouse
     */
    fun render(graphics: GuiGraphicsExtractor, font: Font, mouseX: Int, mouseY: Int, active: Boolean) {
        reportChanges(debouncer.due())
        reportSearches(searches.due())
        val ui = UiGraphics(graphics, font, tokens)
        ui.fillRounded(panel, tokens.card)
        ui.borderRounded(panel, tokens.border)
        ui.fill(Rect(panel.x + 1, panel.y + UiMetrics.TITLE_BAR_HEIGHT, panel.width - 2, 1), tokens.border)
        ui.clipped(Rect(panel.x, panel.y, panel.width, UiMetrics.TITLE_BAR_HEIGHT)) {
            ui.text(titleJson, panel.x + UiMetrics.PANEL_PADDING, panel.y + (UiMetrics.TITLE_BAR_HEIGHT - font.lineHeight + 1) / 2, tokens.cardForeground)
        }

        val layers = stack.toList()
        val areas = layers.map { it.area(scopeFor(it), measurer ?: FontTextMeasurer(font)) }
        val hit = mouseOverlay(areas, mouseX, mouseY)
        val mouseInside = active && viewport.contains(mouseX.toDouble(), mouseY.toDouble()) && hit < 0 && stack.none { it.modal }
        ui.clipped(viewport) {
            root.render(ui, this, if (mouseInside) mouseX else HIDDEN, if (mouseInside) mouseY else HIDDEN)
            focusedWidget?.takeIf { focusVisible }?.takeUnless { focused -> focused.drawsOwnFocus || stack.any { it.containsWidget(focused) } }?.let { focused ->
                val b = (focused.focusFrame ?: focused).bounds
                ui.borderRounded(Rect(b.x - 1, b.y - 1, b.width + 2, b.height + 2), tokens.ring, tokens.radius + 1)
            }
        }
        if (scroll.maxOffset > 0) renderScrollBar(ui)

        layers.forEachIndexed { index, open ->
            ui.nextLayer()
            if (open.modal) ui.fill(scopeFor(open), ThemeColors.withAlpha(BACKDROP, BACKDROP_ALPHA))
            val mouse = active && index == hit
            open.render(ui, this, areas[index], if (mouse) mouseX else HIDDEN, if (mouse) mouseY else HIDDEN)
        }
        tooltip?.let { request ->
            ui.nextLayer()
            TooltipPainter.draw(ui, request, window)
        }
        tooltip = null
        onTop?.let { draw ->
            ui.nextLayer()
            draw(ui)
        }
        onTop = null
    }

    /**
     * Draws the panel's scroll bar along the right edge of the viewport.
     *
     * @param ui the graphics to draw with
     */
    private fun renderScrollBar(ui: UiGraphics) {
        val trackX = panel.right - UiMetrics.PANEL_PADDING / 2 - UiMetrics.SCROLL_BAR_WIDTH / 2
        val content = viewport.height + scroll.maxOffset
        val handleHeight = PanelSizing.handleHeight(viewport.height, content)
        val handleY = viewport.y + (viewport.height - handleHeight) * scroll.offset / scroll.maxOffset
        ui.fillRounded(
            Rect(trackX, handleY, UiMetrics.SCROLL_BAR_WIDTH, handleHeight),
            ThemeColors.withAlpha(tokens.mutedForeground, SCROLL_HANDLE_ALPHA),
            UiMetrics.SCROLL_BAR_WIDTH / 2,
        )
    }

    /**
     * Computes the area of the open popover.
     *
     * @param open the popover
     * @return the popover's area
     */
    private fun popoverArea(open: Popover): Rect = open.area(scopeFor(open), measurer ?: FontTextMeasurer(Minecraft.getInstance().font))

    /**
     * Returns the area a popover is placed in: the window for popovers that are not modal, and
     * for modal ones the nearest overlay container around their owner, or else the visible
     * content of the panel.
     *
     * @param popover the popover
     * @return the area
     */
    private fun scopeFor(popover: Popover): Rect {
        if (!popover.modal) return window
        var current: Widget = popover.owner
        while (true) {
            val parent = WidgetTree.parentOf(root, current.id) ?: return viewport
            if (parent is OverlayContainerWidget) return parent.bounds
            current = parent
        }
    }

    /**
     * Checks whether a point lies on the panel.
     *
     * @param x the x position
     * @param y the y position
     * @return whether the point is inside the panel's area
     */
    fun contains(x: Double, y: Double): Boolean = panel.contains(x, y)

    /**
     * Handles a click: inside an open popover it goes to the popover, and outside it closes the
     * popover; inside the viewport it goes to the tree, and the widget under the mouse takes the
     * focus if it can.
     *
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @param shift whether Shift is held, which the clicked widget reads from [shiftClick]
     * @return whether the click was on the panel or its popover
     */
    fun mouseClicked(x: Double, y: Double, button: Int, shift: Boolean = false): Boolean {
        shiftClick = shift
        focusVisible = false
        for (index in stack.indices.reversed()) {
            val open = stack[index]
            val area = popoverArea(open)
            if (area.contains(x, y)) {
                closeFrom(index + 1)
                if (open.owner.enabled) open.mouseClicked(this, area, x, y, button)
                startDrag(x, y, button)
                return true
            }
            if (open.modal) {
                closeFrom(if (open.dismissOnOutsideClick) index else index + 1)
                return true
            }
            if (open.owner.isOver(x, y) && !open.passesOwnerClicks) {
                closeFrom(index)
                return true
            }
        }
        closeFrom(0)
        if (!panel.contains(x, y)) return false
        if (!viewport.contains(x, y)) return true
        focusedWidget = null
        root.mouseClicked(this, x, y, button)
        startDrag(x, y, button)
        return true
    }

    /**
     * Starts a drag after a left click on the widget that asked for one, or else on the focused
     * widget, if it can be dragged.
     *
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     */
    private fun startDrag(x: Double, y: Double, button: Int) {
        val requested = requestedDrag
        requestedDrag = null
        dragTarget = (requested ?: focusedWidget)?.takeIf { it.draggable && it.isOver(x, y) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT }
    }

    /**
     * Makes a widget receive the mouse movement while the pressed button stays held.
     *
     * @param widget the widget to drag
     */
    override fun beginDrag(widget: Widget) {
        requestedDrag = widget
    }

    /**
     * Passes mouse movement with the button held to the widget a click started a drag on.
     *
     * @param x the mouse x position
     * @param y the mouse y position
     * @return whether a widget is being dragged
     */
    fun mouseDragged(x: Double, y: Double): Boolean {
        val target = dragTarget ?: return false
        if (!ScreenRules.isStillUsable(root, target)) {
            dragTarget = null
            return false
        }
        target.mouseDragged(this, x, y)
        return true
    }

    /**
     * Ends a drag when the mouse button is released.
     */
    fun mouseReleased() {
        dragTarget = null
    }

    /**
     * Passes wheel scrolling to the tree first, and scrolls the panel when no scroll list under the
     * mouse moved.
     *
     * @param x the mouse x position
     * @param y the mouse y position
     * @param amount the scroll amount; positive scrolls up
     * @return whether the mouse was over the panel
     */
    fun mouseScrolled(x: Double, y: Double, amount: Double): Boolean {
        if (stack.isNotEmpty()) {
            val index = stack.indices.lastOrNull { popoverArea(stack[it]).contains(x, y) }
            if (index != null) stack[index].mouseScrolled(this, popoverArea(stack[index]), x, y, amount)
            return true
        }
        if (!panel.contains(x, y)) return false
        if (viewport.contains(x, y) && root.mouseScrolled(this, x, y, amount)) return true
        val before = scroll.offset
        if (scroll.scrollBy((amount * UiMetrics.SCROLL_STEP).roundToInt())) root.offset(0, before - scroll.offset)
        return true
    }

    /**
     * Handles a key: the open popover first, then Tab and Shift+Tab, then the focused widget.
     * Escape closes an open popover and is otherwise left to the screen; Tab also closes it.
     * Enter that the focused widget does not use clicks the submit button of its form.
     *
     * @param event the key event
     * @return whether the key was handled
     */
    fun keyPressed(event: KeyEvent): Boolean {
        focusVisible = true
        val open = popover
        if (open != null) {
            if (open.keyPressed(this, event)) return true
            if (event.isEscape) {
                closePopover()
                return true
            }
            if (event.key() == GLFW.GLFW_KEY_TAB && open.focusRoot == null) closePopover()
        }
        if (event.key() == GLFW.GLFW_KEY_TAB) {
            val scope = stack.lastOrNull { it.focusRoot != null }?.focusRoot ?: root
            focus(FocusOrder.next(scope, focusedWidget, event.hasShiftDown()))
            return true
        }
        if (event.isEscape) return false
        val focused = focusedWidget
        if (focused != null) {
            if (focused.keyPressed(this, event)) return true
            if (offerKey(focused, event)) return true
        }
        if (offerShortcut(event)) return true
        return focused != null && (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) && submitForm(focused)
    }

    /**
     * Offers a key that nothing else used to the widgets of the tree that react to shortcuts, in
     * tree order, until one handles it.
     *
     * @param event the key event
     * @return whether a widget handled the key
     */
    private fun offerShortcut(event: KeyEvent): Boolean {
        val handlers = mutableListOf<ShortcutWidget>()
        WidgetTree.visit(root) { if (it is ShortcutWidget) handlers += it }
        return handlers.any { it.shortcut(this, event) }
    }

    /**
     * Offers a key the focused widget did not use to the containers around it, nearest first,
     * until one handles it.
     *
     * @param focused the focused widget
     * @param event the key event
     * @return whether a container handled the key
     */
    private fun offerKey(focused: Widget, event: KeyEvent): Boolean {
        var current: Widget = focused
        while (true) {
            val parent = WidgetTree.parentOf(root, current.id) ?: return false
            if (parent is KeyInterceptor && parent.descendantKeyPressed(this, focused, event)) return true
            current = parent
        }
    }

    /**
     * Clicks the submit button of the form around a widget.
     *
     * @param widget the widget in which Enter was pressed
     * @return whether a form around the widget has an enabled submit button, which was clicked
     */
    private fun submitForm(widget: Widget): Boolean {
        val form = FormWidget.around(root, widget) ?: return false
        val button = form.submitId?.let { WidgetTree.find(root, it) } as? ButtonWidget ?: return false
        if (!button.enabled) return false
        actionTriggered(button, button.submitsInput)
        return true
    }

    /**
     * Passes a typed character to the open popover, and then to the focused widget.
     *
     * @param event the character event
     * @return whether the character was handled
     */
    fun charTyped(event: CharacterEvent): Boolean =
        popover?.charTyped(this, event) == true || focusedWidget?.charTyped(this, event) == true

    /**
     * Gives a widget the keyboard focus, or clears it, and scrolls a newly focused widget into
     * view.
     *
     * @param widget the widget to focus, or `null` to clear the focus
     */
    override fun focus(widget: Widget?) {
        focusedWidget = widget?.takeIf { it.focusable }
        if (focusedWidget != null) {
            revealPending = true
            requestLayout()
        }
    }

    /**
     * Shows a popover, replacing any open popover.
     *
     * @param popover the popover
     */
    override fun openPopover(popover: Popover) {
        closeFrom(stack.indexOfLast { it.containsWidget(popover.owner) } + 1)
        stack += popover
        popover.focusRoot?.let { root -> FocusOrder.next(root, null, backwards = false)?.let { focus(it) } }
        revealPending = true
        requestLayout()
    }

    /**
     * Closes the top popover.
     */
    override fun closePopover() {
        if (stack.isNotEmpty()) closeFrom(stack.lastIndex)
    }

    /**
     * Closes a popover and every popover above it.
     *
     * @param popover the popover
     */
    override fun closePopover(popover: Popover) {
        val index = stack.indexOf(popover)
        if (index >= 0) closeFrom(index)
    }

    /**
     * Closes the popovers from an index up, topmost first.
     *
     * @param index the index of the lowest popover to close
     */
    private fun closeFrom(index: Int) {
        while (stack.size > index.coerceAtLeast(0)) {
            val closed = stack.removeAt(stack.lastIndex)
            val focused = focusedWidget
            if (focused != null && closed.containsWidget(focused)) {
                focusedWidget = FocusOrder.next(closed.owner, null, backwards = false)?.takeUnless { closed.containsWidget(it) }
            }
            closed.closed(this)
        }
    }

    /**
     * Asks for a tooltip to be drawn above everything at the end of the current frame.
     *
     * @param json the text as component JSON
     * @param anchor the area the tooltip belongs to
     * @param side the side of the anchor the tooltip is shown on
     */
    override fun showTooltip(json: String, anchor: Rect, side: OverlaySide) {
        tooltip = TooltipRequest(json, anchor, side)
    }

    /**
     * Asks for drawing code to run above everything, after the tooltip, at the end of the current
     * frame.
     *
     * @param draw the drawing code
     */
    override fun drawOnTop(draw: (UiGraphics) -> Unit) {
        onTop = draw
    }

    /**
     * Marks the panel for layout before the next frame.
     */
    override fun requestLayout() {
        layoutPending = true
    }

    /**
     * Reports a widget action to the listener, first sending pending changes and, for actions that
     * submit input, marking every input as touched.
     *
     * @param widget the widget
     * @param submitsInput whether the action submits the screen's input
     */
    override fun actionTriggered(widget: Widget, submitsInput: Boolean) {
        if (interceptAction(widget)) return
        OverlayHostWidget.hostOfTrigger(root, widget)?.let { host ->
            if (host.enabled) host.toggle(this)
            return
        }
        reportChanges(debouncer.flush())
        reportSearches(searches.flush())
        if (submitsInput) WidgetTree.touchAll(root)
        listener.actionTriggered(this, widget)
        stack.lastOrNull { it.containsWidget(widget) }?.afterAction(this, widget, submitsInput)
    }

    /**
     * Offers an action to the containers around the widget, nearest first, until one handles it.
     *
     * @param widget the widget whose action fired
     * @return whether a container handled the action
     */
    private fun interceptAction(widget: Widget): Boolean {
        var via = widget
        while (true) {
            val parent = WidgetTree.parentOf(root, via.id) ?: return false
            if (parent is ActionInterceptor && parent.interceptAction(this, widget, via)) return true
            via = parent
        }
    }

    /**
     * Finds a widget of the tree by id.
     *
     * @param id the id of the widget
     * @return the widget, or `null` if the tree has none with that id
     */
    override fun widget(id: String): Widget? = WidgetTree.find(root, id)

    /**
     * Reports a changed input to the listener, at once or after the player paused typing. A report
     * at once replaces a report that still waits for a pause.
     *
     * @param widget the input
     * @param immediate whether to report at once
     */
    override fun valueChanged(widget: Widget, immediate: Boolean) {
        if (immediate) {
            debouncer.cancel(widget.id)
            listener.valueChanged(this, widget)
        } else {
            debouncer.changed(widget.id)
        }
    }

    /**
     * Records a changed combobox query, which is reported after the player paused typing.
     *
     * @param widget the combobox
     * @param query the query
     */
    override fun searchChanged(widget: Widget, query: String) {
        pendingQueries[widget.id] = query
        searches.changed(widget.id)
    }

    /**
     * Reports the queries of comboboxes that are still in the tree.
     *
     * @param ids the ids of the comboboxes
     */
    private fun reportSearches(ids: List<String>) {
        ids.forEach { id ->
            val query = pendingQueries.remove(id) ?: return@forEach
            WidgetTree.find(root, id)?.let { listener.searchChanged(this, it, query) }
        }
    }

    /**
     * Reports the changes of inputs that are still in the tree.
     *
     * @param ids the ids of the changed inputs
     */
    private fun reportChanges(ids: List<String>) {
        ids.forEach { id -> WidgetTree.find(root, id)?.let { listener.valueChanged(this, it) } }
    }

    /**
     * Reports a close request to the listener, which decides what is shown next. Does nothing if
     * the panel is not closable.
     */
    fun requestClose() {
        if (closable) listener.closeRequested(this)
    }

    /**
     * Reacts to a change inside the tree: drops the focus and the open popover if their widgets
     * left the tree or were disabled, and lays the tree out again.
     */
    fun treeChanged() {
        focusedWidget?.let { if (!ScreenRules.isStillUsable(root, it)) focusedWidget = null }
        val gone = stack.indexOfFirst { !ScreenRules.isStillUsable(root, it.owner) }
        if (gone >= 0) closeFrom(gone)
        requestLayout()
    }

    /**
     * Returns the input values of every input widget of the tree.
     *
     * @return the input values, in tree order
     */
    fun inputValues() = WidgetTree.inputValues(root)

    /**
     * Holds the layout and animation constants of panels.
     */
    private companion object {
        /**
         * A mouse position that no widget is under.
         */
        const val HIDDEN: Int = Int.MIN_VALUE / 2

        /**
         * How long a sheet takes to slide in, in milliseconds.
         */
        const val SLIDE_MILLIS: Long = 160

        /**
         * How long a text input's value must stay unchanged before its change is reported.
         */
        const val CHANGE_DELAY_MILLIS: Long = 250

        /**
         * The space kept around a widget that is scrolled into view.
         */
        const val FOCUS_MARGIN: Int = 4

        /**
         * The opacity of the panel's scroll bar handle, relative to the muted foreground colour.
         */
        const val SCROLL_HANDLE_ALPHA: Float = 0.4f

        /**
         * The colour of the backdrop below modal overlays.
         */
        const val BACKDROP: Int = -0x1000000

        /**
         * The opacity of the backdrop below modal overlays.
         */
        const val BACKDROP_ALPHA: Float = 0.5f
    }
}

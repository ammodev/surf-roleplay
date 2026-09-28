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
import dev.slne.surf.roleplay.fabric.ui.widget.Popover
import dev.slne.surf.roleplay.fabric.ui.widget.ScrollListWidget
import dev.slne.surf.roleplay.fabric.ui.widget.UiContext
import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
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
            popover = null
            requestLayout()
        }

    /**
     * The widget that receives keyboard input, or `null` if none does.
     */
    override var focusedWidget: Widget? = null
        private set

    /**
     * The open popover, or `null` if none is open.
     */
    override var popover: Popover? = null
        private set

    /**
     * The widget that receives mouse movement while the button is held, or `null` if none does.
     */
    private var dragTarget: Widget? = null

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
    private var measurer: FontTextMeasurer? = null

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
    fun layoutIfNeeded(font: Font, width: Int, height: Int) {
        val slidingNow = sliding()
        val slideEnded = wasSliding && !slidingNow
        wasSliding = slidingNow
        if (!layoutPending && window.width == width && window.height == height && !slidingNow && !slideEnded) return
        window = Rect(0, 0, width, height)
        layout(font)
        if (revealPending) {
            revealPending = false
            if (revealInLists()) layout(font)
            if (revealInPanel()) layout(font)
        }
    }

    /**
     * Lays the tree out at its full height and places the panel and the viewport for the
     * presentation, capping the panel at the window and scrolling the rest.
     *
     * @param font the font texts are measured with
     */
    private fun layout(font: Font) {
        layoutPending = false
        val measurer = FontTextMeasurer(font).also { this.measurer = it }
        val box = root.createLayout(measurer)
        val preferred = FlexLayout.measure(box)
        val chromeX = 2 * UiMetrics.PANEL_PADDING
        val chromeY = 2 * UiMetrics.PANEL_PADDING + UiMetrics.TITLE_BAR_HEIGHT
        val sheet = style.presentation == Presentation.SHEET
        val margin = if (sheet) 0 else UiMetrics.SCREEN_MARGIN
        val maxWidth = (window.width - 2 * margin - chromeX).coerceAtLeast(0)
        val maxHeight = (window.height - 2 * margin - chromeY).coerceAtLeast(0)
        val verticalSheet = sheet && (style.sheetEdge == SheetEdge.LEFT || style.sheetEdge == SheetEdge.RIGHT)
        val horizontalSheet = sheet && !verticalSheet

        val titleWidth = font.width(ScreenText.parse(titleJson)) + chromeX
        val contentWidth = when {
            horizontalSheet || root.width.mode == SizeMode.GROW -> maxWidth
            else -> preferred.width.coerceAtMost(maxWidth)
        }
        val contentHeight = PanelSizing.contentHeight(root.height.mode, preferred.height, maxHeight)
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
     * Scrolls the scroll lists around the focused widget, innermost first, so that the widget is
     * visible inside them.
     *
     * @return whether a list scrolled, in which case the panel must be laid out again
     */
    private fun revealInLists(): Boolean {
        val focused = focusedWidget ?: popover?.owner ?: return false
        var moved = false
        var current: Widget = focused
        while (true) {
            val parent = WidgetTree.parentOf(root, current.id) ?: break
            if (parent is ScrollListWidget) moved = parent.ensureVisible(focused) || moved
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
        val focused = focusedWidget ?: popover?.owner ?: return false
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

        val open = popover
        val openArea = open?.area(window, FontTextMeasurer(font))
        val mouseInside = active && viewport.contains(mouseX.toDouble(), mouseY.toDouble()) &&
            !(openArea != null && openArea.contains(mouseX.toDouble(), mouseY.toDouble()))
        ui.clipped(viewport) {
            root.render(ui, this, if (mouseInside) mouseX else HIDDEN, if (mouseInside) mouseY else HIDDEN)
            focusedWidget?.takeUnless { it.drawsOwnFocus }?.let { focused ->
                val b = (focused.focusFrame ?: focused).bounds
                ui.borderRounded(Rect(b.x - 1, b.y - 1, b.width + 2, b.height + 2), tokens.ring, tokens.radius + 1)
            }
        }
        if (scroll.maxOffset > 0) renderScrollBar(ui)

        if (open != null && openArea != null) {
            ui.nextLayer()
            open.render(ui, this, openArea, if (active) mouseX else HIDDEN, if (active) mouseY else HIDDEN)
        }
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
    private fun popoverArea(open: Popover): Rect = open.area(window, measurer ?: FontTextMeasurer(Minecraft.getInstance().font))

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
     * @return whether the click was on the panel or its popover
     */
    fun mouseClicked(x: Double, y: Double, button: Int): Boolean {
        val open = popover
        if (open != null) {
            val area = popoverArea(open)
            if (area.contains(x, y)) {
                if (open.owner.enabled) open.mouseClicked(this, area, x, y, button)
                return true
            }
            popover = null
            if (open.owner.isOver(x, y)) return true
        }
        if (!panel.contains(x, y)) return false
        if (!viewport.contains(x, y)) return true
        focusedWidget = null
        root.mouseClicked(this, x, y, button)
        dragTarget = focusedWidget?.takeIf { it.draggable && it.isOver(x, y) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT }
        return true
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
        popover?.let { open ->
            val area = popoverArea(open)
            if (area.contains(x, y)) open.mouseScrolled(area, amount)
            return true
        }
        if (!panel.contains(x, y)) return false
        if (viewport.contains(x, y) && root.mouseScrolled(this, x, y, amount)) return true
        if (scroll.scrollBy((amount * UiMetrics.SCROLL_STEP).roundToInt())) requestLayout()
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
        val open = popover
        if (open != null) {
            if (open.keyPressed(this, event)) return true
            if (event.isEscape) {
                popover = null
                return true
            }
            if (event.key() == GLFW.GLFW_KEY_TAB) popover = null
        }
        if (event.key() == GLFW.GLFW_KEY_TAB) {
            focus(FocusOrder.next(root, focusedWidget, event.hasShiftDown()))
            return true
        }
        if (event.isEscape) return false
        val focused = focusedWidget ?: return false
        if (focused.keyPressed(this, event)) return true
        return (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) && submitForm(focused)
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
        this.popover = popover
        revealPending = true
        requestLayout()
    }

    /**
     * Closes the open popover.
     */
    override fun closePopover() {
        popover = null
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
        reportChanges(debouncer.flush())
        reportSearches(searches.flush())
        if (submitsInput) WidgetTree.touchAll(root)
        listener.actionTriggered(this, widget)
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
        popover?.let { if (!ScreenRules.isStillUsable(root, it.owner)) popover = null }
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
    }
}

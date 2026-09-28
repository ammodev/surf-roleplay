package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.screen.ScreenRules
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.text.ScreenText
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeTokens
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.fabric.ui.widget.ButtonWidget
import dev.slne.surf.roleplay.fabric.ui.widget.DropdownWidget
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
     * Called when the player activates an enabled button.
     *
     * @param panel the panel
     * @param button the activated button
     */
    fun buttonClicked(panel: ScreenPanel, button: ButtonWidget)

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
            dropdown = null
            requestLayout()
        }

    /**
     * The widget that receives keyboard input, or `null` if none does.
     */
    override var focusedWidget: Widget? = null
        private set

    /**
     * The dropdown whose option list is open, or `null` if none is.
     */
    private var dropdown: DropdownWidget? = null

    /**
     * The option highlighted in the open dropdown list, for keyboard selection.
     */
    private var highlighted: Int = 0

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
     * The mouse position of the last frame, used to move the dropdown highlight only when the mouse
     * moves.
     */
    private var lastMouse: Pair<Int, Int> = Int.MIN_VALUE to Int.MIN_VALUE

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
        val measurer = FontTextMeasurer(font)
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
        val focused = focusedWidget ?: dropdown ?: return false
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
        val focused = focusedWidget ?: dropdown ?: return false
        val before = scroll.offset
        val top = focused.bounds.y - (viewport.y - scroll.offset)
        scroll.ensureVisible(top - FOCUS_MARGIN, top + focused.bounds.height + FOCUS_MARGIN)
        return scroll.offset != before
    }

    /**
     * Draws the panel, its content, the scroll bar, the focus ring and any open dropdown list.
     *
     * @param graphics the GUI graphics of this frame
     * @param font the font texts are drawn with
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param active whether this panel receives input; inactive panels ignore the mouse
     */
    fun render(graphics: GuiGraphicsExtractor, font: Font, mouseX: Int, mouseY: Int, active: Boolean) {
        reportChanges(debouncer.due())
        val ui = UiGraphics(graphics, font, tokens)
        ui.fillRounded(panel, tokens.card)
        ui.borderRounded(panel, tokens.border)
        ui.fill(Rect(panel.x + 1, panel.y + UiMetrics.TITLE_BAR_HEIGHT, panel.width - 2, 1), tokens.border)
        ui.clipped(Rect(panel.x, panel.y, panel.width, UiMetrics.TITLE_BAR_HEIGHT)) {
            ui.text(titleJson, panel.x + UiMetrics.PANEL_PADDING, panel.y + (UiMetrics.TITLE_BAR_HEIGHT - font.lineHeight + 1) / 2, tokens.cardForeground)
        }

        val open = dropdown
        val mouseInside = active && viewport.contains(mouseX.toDouble(), mouseY.toDouble()) &&
            !(open != null && dropdownList(open).contains(mouseX.toDouble(), mouseY.toDouble()))
        ui.clipped(viewport) {
            root.render(ui, this, if (mouseInside) mouseX else HIDDEN, if (mouseInside) mouseY else HIDDEN)
            focusedWidget?.let { focused ->
                val b = focused.bounds
                ui.borderRounded(Rect(b.x - 1, b.y - 1, b.width + 2, b.height + 2), tokens.ring, tokens.radius + 1)
            }
        }
        if (scroll.maxOffset > 0) renderScrollBar(ui)

        if (open != null) {
            ui.nextLayer()
            renderDropdownList(ui, open, if (active) mouseX else HIDDEN, if (active) mouseY else HIDDEN)
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
     * Draws the option list of the open dropdown.
     *
     * @param ui the graphics to draw with
     * @param open the open dropdown
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    private fun renderDropdownList(ui: UiGraphics, open: DropdownWidget, mouseX: Int, mouseY: Int) {
        val list = dropdownList(open)
        ui.fillRounded(list, tokens.popover)
        val mouseMoved = lastMouse != (mouseX to mouseY)
        lastMouse = mouseX to mouseY
        open.options.forEachIndexed { index, option ->
            val row = optionRow(list, index)
            if (mouseMoved && row.contains(mouseX.toDouble(), mouseY.toDouble())) highlighted = index
            val lit = index == highlighted
            if (lit) ui.fillRounded(row, tokens.accent, (tokens.radius - 1).coerceAtLeast(0))
            if (option.value == open.selected) ui.fill(Rect(row.x + 2, row.y + 3, 2, row.height - 6), tokens.ring)
            ui.clipped(row) {
                ui.text(option.label, row.x + UiMetrics.WIDGET_PADDING, row.y + (OPTION_HEIGHT - ui.lineHeight + 1) / 2, if (lit) tokens.accentForeground else tokens.popoverForeground)
            }
        }
        ui.borderRounded(list, tokens.border)
    }

    /**
     * Computes the area of one option row of a dropdown list.
     *
     * @param list the area of the list
     * @param index the option index
     * @return the row's area
     */
    private fun optionRow(list: Rect, index: Int) = Rect(list.x + 1, list.y + 1 + index * OPTION_HEIGHT, list.width - 2, OPTION_HEIGHT)

    /**
     * Computes the area of a dropdown's option list: below the dropdown, or above it if the list
     * would leave the window.
     *
     * @param open the dropdown
     * @return the area of the list
     */
    private fun dropdownList(open: DropdownWidget): Rect {
        val listHeight = open.options.size * OPTION_HEIGHT + 2
        val below = open.bounds.bottom
        val y = if (below + listHeight > window.height && open.bounds.y - listHeight >= 0) open.bounds.y - listHeight else below
        return Rect(open.bounds.x, y, open.bounds.width, listHeight)
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
     * Handles a click: on an open dropdown list it selects an option or closes the list; inside
     * the viewport it goes to the tree, and the widget under the mouse takes the focus if it can.
     *
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on the panel or its dropdown list
     */
    fun mouseClicked(x: Double, y: Double, button: Int): Boolean {
        val open = dropdown
        if (open != null) {
            val list = dropdownList(open)
            if (open.enabled && list.contains(x, y)) {
                val index = ((y - list.y - 1) / OPTION_HEIGHT).toInt()
                open.options.getOrNull(index)?.let { open.choose(it.value, this) }
            }
            dropdown = null
            return true
        }
        if (!panel.contains(x, y)) return false
        if (!viewport.contains(x, y)) return true
        focusedWidget = null
        root.mouseClicked(this, x, y, button)
        return true
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
        if (dropdown != null) return true
        if (!panel.contains(x, y)) return false
        if (viewport.contains(x, y) && root.mouseScrolled(this, x, y, amount)) return true
        if (scroll.scrollBy((amount * UiMetrics.SCROLL_STEP).roundToInt())) requestLayout()
        return true
    }

    /**
     * Handles a key: the open dropdown list first, then Tab and Shift+Tab, then the focused
     * widget. Escape is left to the screen unless it closes an open dropdown list.
     *
     * @param event the key event
     * @return whether the key was handled
     */
    fun keyPressed(event: KeyEvent): Boolean {
        val open = dropdown
        if (open != null) return dropdownKey(open, event)
        if (event.key() == GLFW.GLFW_KEY_TAB) {
            focus(FocusOrder.next(root, focusedWidget, event.hasShiftDown()))
            return true
        }
        if (event.isEscape) return false
        return focusedWidget?.keyPressed(this, event) == true
    }

    /**
     * Handles a key while a dropdown list is open.
     *
     * @param open the open dropdown
     * @param event the key event
     * @return `true`, because an open list takes every key
     */
    private fun dropdownKey(open: DropdownWidget, event: KeyEvent): Boolean {
        when (event.key()) {
            GLFW.GLFW_KEY_UP -> highlighted = (highlighted - 1).coerceAtLeast(0)
            GLFW.GLFW_KEY_DOWN -> highlighted = (highlighted + 1).coerceAtMost(open.options.lastIndex)
            GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_SPACE -> {
                open.options.getOrNull(highlighted)?.let { open.choose(it.value, this) }
                dropdown = null
            }

            GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_TAB -> dropdown = null
        }
        return true
    }

    /**
     * Passes a typed character to the focused widget.
     *
     * @param event the character event
     * @return whether the character was handled
     */
    fun charTyped(event: CharacterEvent): Boolean = dropdown == null && focusedWidget?.charTyped(this, event) == true

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
     * Opens a dropdown's option list with its selected option highlighted.
     *
     * @param dropdown the dropdown
     */
    override fun openDropdown(dropdown: DropdownWidget) {
        this.dropdown = dropdown
        highlighted = dropdown.options.indexOfFirst { it.value == dropdown.selected }.coerceAtLeast(0)
        revealPending = true
        requestLayout()
    }

    /**
     * Closes the open dropdown option list.
     */
    override fun closeDropdown() {
        dropdown = null
    }

    /**
     * Marks the panel for layout before the next frame.
     */
    override fun requestLayout() {
        layoutPending = true
    }

    /**
     * Reports a button activation to the listener.
     *
     * @param button the activated button
     */
    override fun buttonClicked(button: ButtonWidget) {
        reportChanges(debouncer.flush())
        if (button.submitsInput) WidgetTree.touchAll(root)
        listener.buttonClicked(this, button)
    }

    /**
     * Reports a changed input to the listener, at once or after the player paused typing.
     *
     * @param widget the input
     * @param immediate whether to report at once
     */
    override fun valueChanged(widget: Widget, immediate: Boolean) {
        if (immediate) listener.valueChanged(this, widget) else debouncer.changed(widget.id)
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
     * Reacts to a change inside the tree: drops the focus and the open dropdown list if their
     * widgets left the tree or were disabled, and lays the tree out again.
     */
    fun treeChanged() {
        focusedWidget?.let { if (!ScreenRules.isStillUsable(root, it)) focusedWidget = null }
        dropdown?.let { if (!ScreenRules.isStillUsable(root, it)) dropdown = null }
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
         * The height of one row of a dropdown option list.
         */
        const val OPTION_HEIGHT: Int = 14

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

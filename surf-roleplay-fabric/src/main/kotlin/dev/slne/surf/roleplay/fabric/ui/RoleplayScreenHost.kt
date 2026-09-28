package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.screen.ScreenRules
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.text.ScreenText
import dev.slne.surf.roleplay.fabric.ui.theme.RoleplayTheme
import dev.slne.surf.roleplay.fabric.ui.widget.ButtonWidget
import dev.slne.surf.roleplay.fabric.ui.widget.DropdownWidget
import dev.slne.surf.roleplay.fabric.ui.widget.UiContext
import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

/**
 * Receives what the player does on a [RoleplayScreenHost].
 */
interface ScreenHostListener {
    /**
     * Called when the player clicks an enabled button.
     *
     * @param host the screen
     * @param button the clicked button
     */
    fun buttonClicked(host: RoleplayScreenHost, button: ButtonWidget)

    /**
     * Called when the player closes a closable screen with Escape.
     *
     * @param host the screen
     */
    fun closeRequested(host: RoleplayScreenHost)
}

/**
 * A Minecraft screen that shows a widget tree in a roleplay panel with a title bar.
 *
 * The tree is laid out within the window, centered, and laid out again whenever the window
 * changes size or a widget asks for it.
 *
 * @param titleJson the title as component JSON
 * @property root the root of the widget tree
 * @property closable whether Escape closes the screen
 * @property listener the receiver of clicks and close requests
 */
class RoleplayScreenHost(
    val titleJson: String,
    root: Widget,
    val closable: Boolean,
    private val listener: ScreenHostListener,
) : Screen(ScreenText.parse(titleJson)), UiContext {

    /**
     * The root of the widget tree. Replacing it drops the focus and lays the screen out again.
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
     * Whether the tree must be laid out before the next frame.
     */
    private var layoutPending: Boolean = true

    /**
     * The area of the panel, including the title bar, set by the last layout.
     */
    private var panel: Rect = Rect.EMPTY

    /**
     * The text on the system clipboard.
     */
    override var clipboard: String
        get() = minecraft.keyboardHandler.clipboard
        set(value) {
            minecraft.keyboardHandler.clipboard = value
        }

    /**
     * Lays the tree out whenever the screen is initialised or resized.
     */
    override fun init() {
        requestLayout()
    }

    /**
     * Lays the tree out within the window, below the title bar, and computes the panel around it.
     */
    private fun layout() {
        layoutPending = false
        val inset = RoleplayTheme.SCREEN_MARGIN + RoleplayTheme.PANEL_PADDING
        val area = Rect(
            inset,
            inset + RoleplayTheme.TITLE_BAR_HEIGHT,
            (width - 2 * inset).coerceAtLeast(0),
            (height - 2 * inset - RoleplayTheme.TITLE_BAR_HEIGHT).coerceAtLeast(0),
        )
        val box = root.createLayout(FontTextMeasurer(font))
        FlexLayout.layoutRoot(box, area)
        root.applyLayout()

        val content = root.bounds
        val titleWidth = font.width(title) + 2 * RoleplayTheme.PANEL_PADDING
        val panelWidth = maxOf(content.width + 2 * RoleplayTheme.PANEL_PADDING, titleWidth)
        panel = Rect(
            content.x + content.width / 2 - panelWidth / 2,
            content.y - RoleplayTheme.PANEL_PADDING - RoleplayTheme.TITLE_BAR_HEIGHT,
            panelWidth,
            content.height + 2 * RoleplayTheme.PANEL_PADDING + RoleplayTheme.TITLE_BAR_HEIGHT,
        )
    }

    /**
     * Draws the panel, the title, the widget tree and any open dropdown list.
     *
     * @param graphics the GUI graphics of this frame
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param partialTick the partial tick
     */
    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (layoutPending) layout()
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
        val ui = UiGraphics(graphics, font)

        ui.fill(panel, RoleplayTheme.PANEL)
        ui.fill(Rect(panel.x, panel.y, panel.width, RoleplayTheme.TITLE_BAR_HEIGHT), RoleplayTheme.TITLE_BAR)
        ui.border(panel, RoleplayTheme.PANEL_BORDER)
        graphics.text(font, title, panel.x + RoleplayTheme.PANEL_PADDING, panel.y + (RoleplayTheme.TITLE_BAR_HEIGHT - font.lineHeight + 1) / 2, RoleplayTheme.TEXT, false)

        val open = dropdown
        val hideMouse = open != null && dropdownList(open).contains(mouseX.toDouble(), mouseY.toDouble())
        root.render(ui, this, if (hideMouse) HIDDEN else mouseX, if (hideMouse) HIDDEN else mouseY)

        if (open != null) {
            ui.nextLayer()
            renderDropdownList(ui, open, mouseX, mouseY)
        }
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
        ui.fill(list, RoleplayTheme.WIDGET)
        open.options.forEachIndexed { index, option ->
            val row = Rect(list.x, list.y + index * OPTION_HEIGHT, list.width, OPTION_HEIGHT)
            if (row.contains(mouseX.toDouble(), mouseY.toDouble())) ui.fill(row, RoleplayTheme.WIDGET_HOVER)
            if (option.value == open.selected) ui.fill(Rect(row.x, row.y, 2, row.height), RoleplayTheme.ACCENT)
            ui.clipped(row) {
                ui.text(option.label, row.x + RoleplayTheme.WIDGET_PADDING, row.y + (OPTION_HEIGHT - font.lineHeight + 1) / 2, RoleplayTheme.TEXT)
            }
        }
        ui.border(list, RoleplayTheme.ACCENT)
    }

    /**
     * Computes the area of a dropdown's option list: below the dropdown, or above it if the list
     * would leave the window.
     *
     * @param open the dropdown
     * @return the area of the list
     */
    private fun dropdownList(open: DropdownWidget): Rect {
        val listHeight = open.options.size * OPTION_HEIGHT
        val below = open.bounds.bottom
        val y = if (below + listHeight > height && open.bounds.y - listHeight >= 0) open.bounds.y - listHeight else below
        return Rect(open.bounds.x, y, open.bounds.width, listHeight)
    }

    /**
     * Handles a click: on an open dropdown list it selects an option or closes the list, and
     * otherwise it clears the focus and passes the click to the tree.
     *
     * @param event the mouse event
     * @param doubleClick whether the click is a double click
     * @return whether the click was handled
     */
    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val open = dropdown
        if (open != null) {
            val list = dropdownList(open)
            if (open.enabled && list.contains(event.x(), event.y())) {
                val index = ((event.y() - list.y) / OPTION_HEIGHT).toInt()
                open.options.getOrNull(index)?.let { open.selected = it.value }
            }
            dropdown = null
            return true
        }
        focusedWidget = null
        return root.mouseClicked(this, event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick)
    }

    /**
     * Passes mouse wheel scrolling to the tree.
     *
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param scrollX the horizontal scroll amount
     * @param scrollY the vertical scroll amount
     * @return whether the scrolling was handled
     */
    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (dropdown != null) return true
        return root.mouseScrolled(this, mouseX, mouseY, scrollY)
    }

    /**
     * Passes a key to the focused widget. Escape closes an open dropdown list first, and
     * otherwise follows the screen's closability.
     *
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.isEscape && dropdown != null) {
            dropdown = null
            return true
        }
        if (!event.isEscape && focusedWidget?.keyPressed(this, event) == true) return true
        return super.keyPressed(event)
    }

    /**
     * Passes a typed character to the focused widget.
     *
     * @param event the character event
     * @return whether the character was handled
     */
    override fun charTyped(event: CharacterEvent): Boolean = focusedWidget?.charTyped(this, event) == true

    /**
     * Only lets Escape close the screen if it is closable.
     *
     * @return whether Escape closes the screen
     */
    override fun shouldCloseOnEsc(): Boolean = closable

    /**
     * Reports a close request to the listener, which decides which screen is shown next. The
     * screen does not close itself.
     */
    override fun onClose() {
        listener.closeRequested(this)
    }

    /**
     * Keeps the game running while the screen is open.
     *
     * @return `false`
     */
    override fun isPauseScreen(): Boolean = false

    /**
     * Gives a widget the keyboard focus, or clears it.
     *
     * @param widget the widget to focus, or `null` to clear the focus
     */
    override fun focus(widget: Widget?) {
        focusedWidget = widget?.takeIf { it.focusable }
    }

    /**
     * Opens a dropdown's option list.
     *
     * @param dropdown the dropdown
     */
    override fun openDropdown(dropdown: DropdownWidget) {
        this.dropdown = dropdown
    }

    /**
     * Closes the open dropdown option list.
     */
    override fun closeDropdown() {
        dropdown = null
    }

    /**
     * Marks the tree for layout before the next frame.
     */
    override fun requestLayout() {
        layoutPending = true
    }

    /**
     * Reports a button click to the listener.
     *
     * @param button the clicked button
     */
    override fun buttonClicked(button: ButtonWidget) {
        listener.buttonClicked(this, button)
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
     * Holds layout constants of the host.
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
    }
}

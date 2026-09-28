package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.SizeMode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * Matches queries of a command menu against items.
 */
object CommandFilter {

    /**
     * Checks whether an item matches a query: every word of the query must occur, ignoring case,
     * in the item's plain text or in one of its keywords. An empty query matches every item.
     *
     * @param query the query
     * @param text the plain text of the item
     * @param keywords the keywords of the item
     * @return whether the item matches
     */
    fun matches(query: String, text: String, keywords: List<String>): Boolean {
        val words = query.trim().lowercase().split(' ').filter { it.isNotEmpty() }
        if (words.isEmpty()) return true
        val haystack = (listOf(text) + keywords).joinToString(" ").lowercase()
        return words.all { it in haystack }
    }
}

/**
 * A command menu: an input whose query filters the items below it, one of which is highlighted
 * and chosen with Enter. Groups without matching items, separators while a query is typed, and
 * the empty text while something matches are hidden. A command that reports its searches leaves
 * the filtering to the server.
 *
 * @param id the id of the widget
 * @property notifySearch whether the command reports its query instead of filtering itself
 */
class CommandWidget(id: String, val notifySearch: Boolean) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }

    /**
     * The typed query.
     */
    var query: String = ""
        private set

    /**
     * The highlighted item, or `null` if no item is shown.
     */
    var highlighted: CommandItemWidget? = null
        private set

    /**
     * Returns the items of the command in order.
     *
     * @return the items
     */
    fun items(): List<CommandItemWidget> {
        val found = mutableListOf<CommandItemWidget>()
        WidgetTree.visit(this) { if (it is CommandItemWidget) found += it }
        return found
    }

    /**
     * Returns the items that are shown and can be chosen, in order.
     *
     * @return the items
     */
    fun choosable(): List<CommandItemWidget> = items().filter { !it.hidden && it.enabled }

    /**
     * Links the parts to this command and applies the current query.
     */
    fun link() {
        WidgetTree.visit(this) {
            when (it) {
                is CommandInputWidget -> it.command = this
                is CommandItemWidget -> it.command = this
            }
        }
        applyFilter()
    }

    /**
     * Records a new query, reports it or filters the items by it, and moves the highlight to the
     * first choosable item if the highlighted one is no longer shown.
     *
     * @param context the screen showing the command
     * @param text the query
     */
    fun queryChanged(context: UiContext, text: String) {
        if (text == query) return
        query = text
        if (notifySearch) context.searchChanged(this, text)
        applyFilter()
        context.requestLayout()
    }

    /**
     * Shows and hides the parts for the current query.
     */
    private fun applyFilter() {
        val searching = query.isNotBlank() && !notifySearch
        items().forEach { it.hidden = searching && !CommandFilter.matches(query, PlainText.of(it.text), it.keywords) }
        WidgetTree.visit(this) { widget ->
            when (widget) {
                is CommandGroupWidget -> widget.hidden = widget.childList.filterIsInstance<CommandItemWidget>().none { !it.hidden }
                is CommandSeparatorWidget -> widget.hidden = searching
            }
        }
        val anyShown = items().any { !it.hidden }
        WidgetTree.visit(this) { if (it is CommandEmptyWidget) it.hidden = anyShown }
        if (highlighted?.let { it.hidden || !it.enabled } != false) highlighted = choosable().firstOrNull()
    }

    /**
     * Highlights an item.
     *
     * @param item the item
     */
    fun highlight(item: CommandItemWidget) {
        highlighted = item
    }

    /**
     * Moves the highlight through the choosable items.
     *
     * @param step `1` for the next item, `-1` for the previous one, stopping at the ends
     */
    fun moveHighlight(step: Int) {
        val items = choosable()
        if (items.isEmpty()) return
        val index = items.indexOf(highlighted)
        highlighted = if (index < 0) items.first() else items[(index + step).coerceIn(0, items.lastIndex)]
    }

    /**
     * Chooses the highlighted item.
     *
     * @param context the screen showing the command
     * @return whether an item was chosen
     */
    fun chooseHighlighted(context: UiContext): Boolean {
        val item = highlighted ?: return false
        item.choose(context)
        return true
    }

    /**
     * Links the parts again, as patches may have replaced them, then creates the layout box.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        link()
        return super.createLayout(measurer)
    }

    /**
     * Draws the surface, then the parts.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.fillRounded(bounds, ui.tokens.popover)
        super.render(ui, context, mouseX, mouseY)
    }
}

/**
 * The search input of a command menu. It edits the command's query, moves the highlight with Up
 * and Down, and chooses the highlighted item with Enter. Its text is not an input value of the
 * screen.
 *
 * @param id the id of the widget
 * @param placeholder the hint shown while the input is empty, as component JSON
 */
class CommandInputWidget(id: String, placeholder: String) : TextInputWidget(id, TextEditState(), placeholder, icon = "search") {
    init {
        embedded = true
    }

    /**
     * The command the input belongs to, linked by the command.
     */
    var command: CommandWidget? = null

    /**
     * The query is not submitted with the screen.
     */
    override val inputValue: String? get() = null

    /**
     * Returns the input height and a width that grows with the command.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(super.contentSize(measurer).width, HEIGHT)

    /**
     * Draws the input and the line below it across the command.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        super.render(ui, context, mouseX, mouseY)
        ui.fill(Rect(bounds.x, bounds.bottom - 1, bounds.width, 1), ui.tokens.border)
    }

    /**
     * Handles Up, Down and Enter for the command, and editing keys for the query.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        val owner = command
        if (owner != null) {
            when (event.key()) {
                GLFW.GLFW_KEY_DOWN -> return true.also { owner.moveHighlight(1) }
                GLFW.GLFW_KEY_UP -> return true.also { owner.moveHighlight(-1) }
                GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> return owner.chooseHighlighted(context)
            }
        }
        val handled = super.keyPressed(context, event)
        owner?.queryChanged(context, edit.text)
        return handled
    }

    /**
     * Inserts a typed character and updates the command's query.
     *
     * @param context the screen showing the widget
     * @param event the character event
     * @return whether the character was handled
     */
    override fun charTyped(context: UiContext, event: CharacterEvent): Boolean {
        val handled = super.charTyped(context, event)
        command?.queryChanged(context, edit.text)
        return handled
    }

    /**
     * Holds the input height.
     */
    private companion object {
        /**
         * The height of the input.
         */
        const val HEIGHT: Int = 18
    }
}

/**
 * The scrolling list of a command menu, at most [MAX_HEIGHT] tall.
 *
 * @param id the id of the widget
 */
class CommandListWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
        padding = Insets(2, 2, 2, 2)
    }

    /**
     * The list scrolls its children.
     */
    override val scrolls: Boolean get() = true

    /**
     * How far the list is scrolled, in GUI pixels from the top.
     */
    private var scrollOffset: Int = 0

    /**
     * The largest scroll offset allowed by the last layout.
     */
    private var maxScroll: Int = 0

    /**
     * Creates a layout box as tall as the content, but no taller than the largest height.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        val natural = FlexLayout.measure(super.createLayout(measurer))
        val capped = if (height.mode == SizeMode.FIT) Sizing.fixed(natural.height.coerceAtMost(MAX_HEIGHT)) else height
        return LayoutBox(
            width = width,
            height = capped,
            axis = Axis.VERTICAL,
            children = shownChildren.map { it.createLayout(measurer) },
            padding = padding,
            crossAlign = crossAlign,
            scrolls = true,
        ).also { layoutBox = it }
    }

    /**
     * Copies the computed bounds and shifts the children by the scroll offset.
     */
    override fun applyLayout() {
        super.applyLayout()
        val box = layoutBox ?: return
        maxScroll = (box.contentExtent - bounds.height).coerceAtLeast(0)
        scrollOffset = scrollOffset.coerceIn(0, maxScroll)
        shownChildren.forEach { it.offset(0, -scrollOffset) }
    }

    /**
     * Draws the children clipped to the list.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val inside = isOver(mouseX, mouseY)
        ui.clipped(bounds) { super.render(ui, context, if (inside) mouseX else Int.MIN_VALUE / 2, if (inside) mouseY else Int.MIN_VALUE / 2) }
    }

    /**
     * Passes a click inside the list to the children.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether a child handled the click
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean = isOver(x, y) && super.mouseClicked(context, x, y, button)

    /**
     * Scrolls the list while the mouse is over it.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param amount the scroll amount; positive scrolls up
     * @return whether the list moved
     */
    override fun mouseScrolled(context: UiContext, x: Double, y: Double, amount: Double): Boolean {
        if (!isOver(x, y)) return false
        val next = (scrollOffset - (amount * STEP).toInt()).coerceIn(0, maxScroll)
        if (next == scrollOffset) return false
        scrollOffset = next
        context.requestLayout()
        return true
    }

    /**
     * Holds the list metrics.
     */
    companion object {
        /**
         * The largest height of the list.
         */
        const val MAX_HEIGHT: Int = 150

        /**
         * How far one wheel step scrolls.
         */
        private const val STEP: Int = 12
    }
}

/**
 * The text of a command menu shown while no item matches.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 */
class CommandEmptyWidget(id: String, var text: String) : Widget(id) {

    /**
     * Returns the text with generous space above and below.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(measurer.width(text), measurer.lineHeight + 2 * PADDING_Y)

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
     * Holds the spacing.
     */
    private companion object {
        /**
         * The space above and below the text.
         */
        const val PADDING_Y: Int = 12
    }
}

/**
 * A group of command items under a muted heading.
 *
 * @param id the id of the widget
 * @property heading the heading as component JSON, or `null` for none
 */
class CommandGroupWidget(id: String, var heading: String?) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        crossAlign = Align.STRETCH
    }

    /**
     * Leaves room for the heading above the items, then creates the layout box of the group.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        padding = Insets(if (heading != null) HEADING_HEIGHT else 0, 0, 0, 0)
        return super.createLayout(measurer)
    }

    /**
     * Draws the heading, then the items.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        heading?.let { ui.text(it, bounds.x + MenuStyle.PADDING_X, bounds.y + (HEADING_HEIGHT - ui.lineHeight + 1) / 2, ui.tokens.mutedForeground) }
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Replaces the heading.
     *
     * @param json the new heading as component JSON
     */
    override fun applyText(json: String) {
        heading = json
    }

    /**
     * Holds the heading height.
     */
    private companion object {
        /**
         * The height of the heading.
         */
        const val HEADING_HEIGHT: Int = 14
    }
}

/**
 * An item of a command menu: highlighted while it is the command's highlight or under the mouse,
 * and firing a widget action when chosen.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property shortcut a shortcut shown at the end as component JSON, or `null` for none
 * @property keywords further words the query matches the item by
 */
class CommandItemWidget(id: String, var text: String, val icon: String?, val shortcut: String?, val keywords: List<String>) : MenuEntryWidget(id) {

    /**
     * The command the item belongs to, linked by the command.
     */
    var command: CommandWidget? = null

    /**
     * Command items are chosen through the command's input, so they do not take the focus.
     */
    override val focusable: Boolean get() = false

    /**
     * Checks whether the item is highlighted: while it is the command's highlight.
     *
     * @param context the screen showing the entry
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @return whether the item is highlighted
     */
    override fun highlighted(context: UiContext, mouseX: Int, mouseY: Int): Boolean = enabled && command?.highlighted === this

    /**
     * Returns the size of the icon, text and shortcut on one line.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = lineSize(measurer, text, icon != null, shortcut?.let { measurer.width(it) } ?: 0)

    /**
     * Highlights the item under the mouse, then draws the highlight, the icon, the text and the
     * shortcut.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        if (enabled && isOver(mouseX, mouseY)) command?.highlight(this)
        val lit = drawHighlight(ui, context, mouseX, mouseY)
        icon?.let {
            ui.icon(it, Rect(bounds.x + MenuStyle.PADDING_X, bounds.y + (bounds.height - MenuStyle.ICON) / 2, MenuStyle.ICON, MenuStyle.ICON), ui.tokens.mutedForeground)
        }
        drawLine(ui, text, icon != null, shortcut, textColor(ui, lit))
    }

    /**
     * Fires the item's action.
     *
     * @param context the screen showing the item
     */
    override fun choose(context: UiContext) = context.actionTriggered(this, submitsInput = false)

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
 * A line between the parts of a command menu.
 *
 * @param id the id of the widget
 */
class CommandSeparatorWidget(id: String) : Widget(id) {

    /**
     * Returns the height of the line with its space.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(0, 3)

    /**
     * Draws the line through the middle of the widget.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.fill(Rect(bounds.x - 2, bounds.y + 1, bounds.width + 4, 1), ui.tokens.border)
    }
}

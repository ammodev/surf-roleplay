package dev.slne.surf.roleplay.fabric.ui.widget

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.SelectGroup
import dev.slne.surf.roleplay.protocol.screen.SelectOption
import dev.slne.surf.roleplay.protocol.screen.SelectSize
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * A layer drawn above a panel's content and below nothing else, opened by a widget, such as the
 * option list of a select. Clicks outside it close it.
 */
interface Popover {
    /**
     * The widget that opened the popover.
     */
    val owner: Widget

    /**
     * Computes where the popover is drawn.
     *
     * @param window the window area
     * @param measurer the text measurer
     * @return the popover's area
     */
    fun area(window: Rect, measurer: TextMeasurer): Rect

    /**
     * Draws the popover.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the popover
     * @param area the popover's area
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    fun render(ui: UiGraphics, context: UiContext, area: Rect, mouseX: Int, mouseY: Int)

    /**
     * Handles a click inside the popover.
     *
     * @param context the screen showing the popover
     * @param area the popover's area
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     */
    fun mouseClicked(context: UiContext, area: Rect, x: Double, y: Double, button: Int)

    /**
     * Handles wheel scrolling inside the popover.
     *
     * @param area the popover's area
     * @param amount the scroll amount; positive scrolls up
     */
    fun mouseScrolled(area: Rect, amount: Double) = Unit

    /**
     * Whether a click on the owner, outside the popover, closes the popover and still reaches the
     * owner; otherwise it only closes the popover.
     */
    val passesOwnerClicks: Boolean get() = false

    /**
     * Handles a key before the focused widget gets it.
     *
     * @param context the screen showing the popover
     * @param event the key event
     * @return whether the key was handled; unhandled keys go to the focused widget, and Escape
     *         and Tab close the popover
     */
    fun keyPressed(context: UiContext, event: KeyEvent): Boolean

    /**
     * Handles a typed character before the focused widget gets it.
     *
     * @param context the screen showing the popover
     * @param event the character event
     * @return whether the character was handled
     */
    fun charTyped(context: UiContext, event: CharacterEvent): Boolean = false

    /**
     * Handles wheel scrolling inside the popover.
     *
     * @param context the screen showing the popover
     * @param area the popover's area
     * @param x the mouse x position
     * @param y the mouse y position
     * @param amount the scroll amount; positive scrolls up
     */
    fun mouseScrolled(context: UiContext, area: Rect, x: Double, y: Double, amount: Double) = mouseScrolled(area, amount)

    /**
     * Whether the popover is modal: everything below it is dimmed and receives no input.
     */
    val modal: Boolean get() = false

    /**
     * Whether a click outside a modal popover closes it.
     */
    val dismissOnOutsideClick: Boolean get() = true

    /**
     * The widget whose descendants Tab cycles through while the popover is open, or `null` to let
     * Tab close the popover.
     */
    val focusRoot: Widget? get() = null

    /**
     * Checks whether a widget is part of the popover's content.
     *
     * @param widget the widget
     * @return whether the widget is inside the popover
     */
    fun containsWidget(widget: Widget): Boolean = false

    /**
     * Called after the popover was closed, for any reason.
     *
     * @param context the screen that showed the popover
     */
    fun closed(context: UiContext) = Unit

    /**
     * Called after a widget inside the popover triggered an action.
     *
     * @param context the screen showing the popover
     * @param widget the widget
     * @param submitsInput whether the action submitted the screen's input
     */
    fun afterAction(context: UiContext, widget: Widget, submitsInput: Boolean) = Unit
}

/**
 * Places popovers next to the widgets that open them.
 */
object Popovers {
    /**
     * The space between a widget and its popover.
     */
    const val GAP: Int = 2

    /**
     * Places a popover below an anchor, or above it if it does not fit below, keeping it inside
     * the window horizontally.
     *
     * @param anchor the area of the widget that opened the popover
     * @param width the popover width
     * @param height the popover height
     * @param window the window area
     * @return the popover's area
     */
    fun below(anchor: Rect, width: Int, height: Int, window: Rect): Rect {
        val fitsBelow = anchor.bottom + GAP + height <= window.bottom
        val fitsAbove = anchor.y - GAP - height >= window.y
        val y = if (fitsBelow || !fitsAbove) anchor.bottom + GAP else anchor.y - GAP - height
        val x = anchor.x.coerceIn(window.x, (window.right - width).coerceAtLeast(window.x))
        return Rect(x, y, width, height)
    }
}

/**
 * Extracts the plain text of component JSON without Minecraft's text system.
 */
object PlainText {
    /**
     * Returns the plain text of component JSON: its texts and the texts of its children, in
     * order.
     *
     * @param json the component JSON
     * @return the plain text, or the JSON itself if it is not valid JSON
     */
    fun of(json: String): String {
        if (json.isEmpty()) return ""
        val element = try {
            JsonParser.parseString(json)
        } catch (exception: RuntimeException) {
            return json
        }
        return StringBuilder().also { append(element, it) }.toString()
    }

    /**
     * Appends the texts of a component element.
     *
     * @param element the component element
     * @param into the builder
     */
    private fun append(element: JsonElement, into: StringBuilder) {
        when {
            element.isJsonPrimitive -> into.append(element.asString)
            element.isJsonArray -> element.asJsonArray.forEach { append(it, into) }
            element.isJsonObject -> {
                val obj = element.asJsonObject
                obj.get("text")?.takeIf { it.isJsonPrimitive }?.let { into.append(it.asString) }
                obj.get("extra")?.let { append(it, into) }
            }
        }
    }
}

/**
 * The rows of an option list, filtered by a query, with a keyboard highlight and scrolling.
 *
 * @param groups the option groups
 * @property plainText returns the plain text of an option label, which the query is matched
 *           against
 * @property filters whether the query filters the options; otherwise every option is shown
 */
class OptionList(groups: List<SelectGroup>, private val plainText: (String) -> String = PlainText::of, private val filters: Boolean = true) {

    /**
     * One row of an option list.
     */
    sealed interface Row {
        /**
         * The height of the row.
         */
        val height: Int
    }

    /**
     * The heading of a group.
     *
     * @property label the heading as component JSON
     */
    data class Heading(val label: String) : Row {
        /**
         * The height of a heading.
         */
        override val height: Int get() = HEADING_HEIGHT
    }

    /**
     * A line between two groups.
     */
    data object Separator : Row {
        /**
         * The height of a separator.
         */
        override val height: Int get() = SEPARATOR_HEIGHT
    }

    /**
     * An option.
     *
     * @property option the option
     */
    data class Item(val option: SelectOption) : Row {
        /**
         * The height of an option.
         */
        override val height: Int get() = ITEM_HEIGHT
    }

    /**
     * The option groups.
     */
    var groups: List<SelectGroup> = groups
        set(value) {
            field = value
            rebuild()
        }

    /**
     * The text options must contain, ignoring case; empty shows every option.
     */
    var query: String = ""
        set(value) {
            if (field == value) return
            field = value
            rebuild()
            scroll = 0
            highlighted = firstEnabled()
        }

    /**
     * The visible rows.
     */
    var rows: List<Row> = emptyList()
        private set

    /**
     * The index of the highlighted row, or `-1` if none is highlighted.
     */
    var highlighted: Int = -1
        private set

    /**
     * How far the rows are scrolled, in GUI pixels.
     */
    var scroll: Int = 0
        private set

    /**
     * The mouse position of the last frame, used to move the highlight only when the mouse moves.
     */
    private var lastMouse: Pair<Int, Int> = Int.MIN_VALUE to Int.MIN_VALUE

    init {
        rebuild()
    }

    /**
     * Every option of every group, in order.
     */
    val options: List<SelectOption> get() = groups.flatMap { it.options }

    /**
     * The height of all rows with the list's padding.
     */
    val contentHeight: Int get() = rows.sumOf { it.height } + 2 * PADDING

    /**
     * Returns the label of an option.
     *
     * @param value the option value
     * @return the label as component JSON, or `null` if no option has the value
     */
    fun label(value: String): String? = options.firstOrNull { it.value == value }?.label

    /**
     * Builds the visible rows: every group with a matching option, with its heading, and a
     * separator between two groups.
     */
    private fun rebuild() {
        val built = mutableListOf<Row>()
        for (group in groups) {
            val matching = group.options.filter { !filters || query.isEmpty() || plainText(it.label).contains(query, ignoreCase = true) }
            if (matching.isEmpty()) continue
            if (built.isNotEmpty()) built += Separator
            group.label?.let { built += Heading(it) }
            matching.mapTo(built) { Item(it) }
        }
        rows = built
        if (highlighted !in rows.indices || !isSelectable(highlighted)) highlighted = firstEnabled()
    }

    /**
     * Checks whether a row is an enabled option.
     *
     * @param index the row index
     * @return whether the row can be highlighted
     */
    private fun isSelectable(index: Int): Boolean = (rows.getOrNull(index) as? Item)?.option?.enabled == true

    /**
     * Returns the first enabled option row.
     *
     * @return its index, or `-1` if there is none
     */
    private fun firstEnabled(): Int = rows.indices.firstOrNull(::isSelectable) ?: -1

    /**
     * Highlights the option with a value, or the first enabled option if no visible option has it.
     *
     * @param value the option value, or `null`
     */
    fun highlightValue(value: String?) {
        highlighted = rows.indexOfFirst { it is Item && it.option.value == value && it.option.enabled }.takeIf { it >= 0 } ?: firstEnabled()
    }

    /**
     * Moves the highlight to the next or previous enabled option, staying at the ends.
     *
     * @param delta `1` for the next option, `-1` for the previous one
     */
    fun moveHighlight(delta: Int) {
        var index = highlighted
        while (true) {
            index += delta
            if (index !in rows.indices) return
            if (isSelectable(index)) {
                highlighted = index
                return
            }
        }
    }

    /**
     * Highlights the first or last enabled option.
     *
     * @param last whether to highlight the last option
     */
    fun highlightEnd(last: Boolean) {
        highlighted = (if (last) rows.indices.lastOrNull(::isSelectable) else rows.indices.firstOrNull(::isSelectable)) ?: -1
    }

    /**
     * The highlighted option, or `null` if none is highlighted.
     */
    val highlightedOption: SelectOption? get() = (rows.getOrNull(highlighted) as? Item)?.option

    /**
     * Returns the top of a row relative to the unscrolled list.
     *
     * @param index the row index
     * @return the offset of the row's top from the list's top
     */
    private fun rowTop(index: Int): Int = PADDING + rows.take(index).sumOf { it.height }

    /**
     * Returns the row at a vertical position.
     *
     * @param area the list's area
     * @param y the vertical position
     * @return the row index, or `-1` if no row is there
     */
    fun rowAt(area: Rect, y: Double): Int {
        val offset = (y - area.y + scroll).toInt()
        return rows.indices.firstOrNull { offset >= rowTop(it) && offset < rowTop(it) + rows[it].height } ?: -1
    }

    /**
     * Scrolls as little as needed to show the highlighted row.
     *
     * @param viewHeight the height the list is shown in
     */
    fun revealHighlight(viewHeight: Int) {
        if (highlighted < 0) return
        val top = rowTop(highlighted)
        val bottom = top + rows[highlighted].height
        if (top - PADDING < scroll) scroll = top - PADDING
        if (bottom + PADDING > scroll + viewHeight) scroll = bottom + PADDING - viewHeight
        scroll = scroll.coerceIn(0, (contentHeight - viewHeight).coerceAtLeast(0))
    }

    /**
     * Scrolls the list by an amount.
     *
     * @param pixels the distance; positive scrolls down
     * @param viewHeight the height the list is shown in
     */
    fun scrollBy(pixels: Int, viewHeight: Int) {
        scroll = (scroll + pixels).coerceIn(0, (contentHeight - viewHeight).coerceAtLeast(0))
    }

    /**
     * Returns the width the list needs for its widest row.
     *
     * @param measurer the text measurer
     * @return the width in GUI pixels
     */
    fun preferredWidth(measurer: TextMeasurer): Int =
        (options.maxOfOrNull { measurer.width(it.label) } ?: 0) + ITEM_PADDING + CHECK_SPACE + 2 * PADDING

    /**
     * Draws the rows inside an area, or an empty text if no option matches. The highlight follows
     * the mouse when it moves over an enabled option.
     *
     * @param ui the graphics to draw with
     * @param area the list's area
     * @param selected the values of the selected options, which show a check mark
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param emptyText the text shown when no option matches, as component JSON, or `null`
     */
    fun render(ui: UiGraphics, area: Rect, selected: Collection<String>, mouseX: Int, mouseY: Int, emptyText: String?) {
        val tokens = ui.tokens
        val mouseMoved = lastMouse != (mouseX to mouseY)
        lastMouse = mouseX to mouseY
        if (mouseMoved && area.contains(mouseX.toDouble(), mouseY.toDouble())) {
            val under = rowAt(area, mouseY.toDouble())
            if (isSelectable(under)) highlighted = under
        }
        if (rows.isEmpty()) {
            if (emptyText != null) ui.centeredText(emptyText, area, tokens.mutedForeground)
            return
        }
        ui.clipped(area) {
            rows.forEachIndexed { index, row ->
                val top = area.y + rowTop(index) - scroll
                if (top + row.height < area.y || top > area.bottom) return@forEachIndexed
                val rect = Rect(area.x + PADDING, top, area.width - 2 * PADDING, row.height)
                when (row) {
                    is Heading -> ui.text(row.label, rect.x + ITEM_PADDING / 2, rect.y + (rect.height - ui.lineHeight + 1) / 2, tokens.mutedForeground)
                    Separator -> ui.fill(Rect(area.x, rect.y + rect.height / 2, area.width, 1), tokens.border)
                    is Item -> {
                        val lit = index == highlighted
                        val fade: (Int) -> Int = { if (row.option.enabled) it else ui.disabled(it) }
                        if (lit) ui.fillRounded(rect, tokens.accent, (tokens.radius - 1).coerceAtLeast(0))
                        val color = fade(if (lit) tokens.accentForeground else tokens.popoverForeground)
                        ui.text(row.option.label, rect.x + ITEM_PADDING / 2, rect.y + (rect.height - ui.lineHeight + 1) / 2, color)
                        if (row.option.value in selected) {
                            val side = UiMetrics.INLINE_ICON
                            ui.icon("check", Rect(rect.right - side - 2, rect.y + (rect.height - side) / 2, side, side), color)
                        }
                    }
                }
            }
        }
    }

    /**
     * Holds the row sizes.
     */
    companion object {
        /**
         * The height of an option row.
         */
        const val ITEM_HEIGHT: Int = 14

        /**
         * The height of a group heading.
         */
        const val HEADING_HEIGHT: Int = 12

        /**
         * The height of a separator row.
         */
        const val SEPARATOR_HEIGHT: Int = 5

        /**
         * The space around the rows.
         */
        const val PADDING: Int = 2

        /**
         * The horizontal space inside an option row.
         */
        const val ITEM_PADDING: Int = 8

        /**
         * The space an option row keeps for its check mark.
         */
        const val CHECK_SPACE: Int = 16

        /**
         * The largest height of a popover list before it scrolls.
         */
        const val MAX_HEIGHT: Int = 130
    }
}

/**
 * Draws the frame of a popover: its fill and border.
 *
 * @param ui the graphics to draw with
 * @param area the popover's area
 */
internal fun drawPopoverFrame(ui: UiGraphics, area: Rect) {
    ui.fillRounded(area, ui.tokens.popover)
    ui.borderRounded(area, ui.tokens.border)
}

/**
 * A choice of one option from a list that opens below the trigger.
 *
 * @param id the id of the widget
 * @param groups the option groups
 * @property selected the value of the selected option, or `null` if none is selected
 * @property placeholder the text shown while nothing is selected, as component JSON
 * @property size the size of the trigger
 * @property required whether having no selection is invalid
 */
open class SelectWidget(
    id: String,
    groups: List<SelectGroup>,
    var selected: String?,
    var placeholder: String,
    val size: SelectSize,
    val required: Boolean,
) : Widget(id) {

    /**
     * The options, as the popover shows them.
     */
    val list: OptionList = OptionList(groups)

    /**
     * Whether the widget can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * The value of the selected option, or an empty string if none is selected.
     */
    override val inputValue: String get() = selected ?: ""

    /**
     * Whether the select shows itself as invalid: while the server marks it invalid, or once touched while required and empty.
     */
    override val showsInvalid: Boolean get() = serverInvalid || touched && required && selected == null

    /**
     * The height of the trigger.
     */
    private val triggerHeight: Int get() = if (size == SelectSize.SM) SM_HEIGHT else UiMetrics.WIDGET_HEIGHT

    /**
     * Returns the width of the widest option or the placeholder with the chevron, at least the
     * default input width, and the trigger height.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        val widest = maxOf(list.options.maxOfOrNull { measurer.width(it.label) } ?: 0, measurer.width(placeholder))
        return Size(maxOf(UiMetrics.INPUT_WIDTH, widest + 2 * UiMetrics.WIDGET_PADDING + UiMetrics.INLINE_ICON + UiMetrics.ICON_GAP), triggerHeight)
    }

    /**
     * Draws the trigger: the selected option's label or the placeholder, and a chevron.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val hovered = enabled && isOver(mouseX, mouseY)
        ui.fillRounded(bounds, if (hovered) ThemeColors.blend(inputFill(ui), tokens.accent, HOVER_ACCENT) else inputFill(ui))
        ui.borderRounded(bounds, if (showsInvalid) tokens.destructive else tokens.input)
        val fade: (Int) -> Int = { if (enabled) it else ui.disabled(it) }
        val textY = bounds.y + (bounds.height - ui.lineHeight + 1) / 2
        val side = UiMetrics.INLINE_ICON
        ui.clipped(Rect(bounds.x, bounds.y, bounds.width - side - UiMetrics.WIDGET_PADDING, bounds.height)) {
            val label = selected?.let(list::label)
            if (label != null) {
                ui.text(label, bounds.x + UiMetrics.WIDGET_PADDING, textY, fade(tokens.foreground))
            } else {
                ui.text(placeholder, bounds.x + UiMetrics.WIDGET_PADDING, textY, fade(tokens.mutedForeground))
            }
        }
        ui.icon("chevron-down", Rect(bounds.right - UiMetrics.WIDGET_PADDING - side, bounds.y + (bounds.height - side) / 2, side, side), fade(tokens.mutedForeground))
    }

    /**
     * Selects an option chosen by the player and reports the change.
     *
     * @param value the value of the chosen option
     * @param context the screen showing the widget
     */
    fun choose(value: String, context: UiContext) {
        if (list.options.none { it.value == value && it.enabled }) return
        if (value == selected) return
        selected = value
        markChanged(context, immediate = true)
    }

    /**
     * Opens the option list with the selected option highlighted.
     *
     * @param context the screen showing the widget
     */
    fun open(context: UiContext) {
        list.query = ""
        list.highlightValue(selected)
        context.openPopover(SelectPopover(this))
    }

    /**
     * Focuses an enabled select on a left click and opens its list.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this select
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            open(context)
        }
        return true
    }

    /**
     * Opens the list on Enter, Space, Up or Down.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled) return false
        if (!isActivation(event) && event.key() != GLFW.GLFW_KEY_DOWN && event.key() != GLFW.GLFW_KEY_UP) return false
        open(context)
        return true
    }

    /**
     * Selects the option with the given value, or clears the selection for an empty or unknown
     * value.
     *
     * @param value the value of the option to select
     */
    override fun applyValue(value: String) {
        selected = value.takeIf { candidate -> list.options.any { it.value == candidate } }
    }

    /**
     * Replaces the placeholder.
     *
     * @param json the new placeholder as component JSON
     */
    override fun applyText(json: String) {
        placeholder = json
    }

    /**
     * Holds the trigger sizes.
     */
    protected companion object {
        /**
         * The height of a small trigger.
         */
        const val SM_HEIGHT: Int = 16

        /**
         * How far a hovered trigger is blended towards the accent colour.
         */
        const val HOVER_ACCENT: Float = 0.6f
    }
}

/**
 * A plain choice of one option. Up and Down change the selection without opening the list;
 * Enter and Space open it.
 *
 * @param id the id of the widget
 * @param groups the option groups
 * @param selected the value of the selected option, or `null` if none is selected
 * @param size the size of the trigger
 * @param required whether having no selection is invalid
 */
class NativeSelectWidget(id: String, groups: List<SelectGroup>, selected: String?, size: SelectSize, required: Boolean) :
    SelectWidget(id, groups, selected, "", size, required) {

    /**
     * Selects the next or previous enabled option on Down or Up, and opens the list on Enter or
     * Space.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled) return false
        val delta = when (event.key()) {
            GLFW.GLFW_KEY_DOWN -> 1
            GLFW.GLFW_KEY_UP -> -1
            else -> return super.keyPressed(context, event)
        }
        list.query = ""
        list.highlightValue(selected)
        if (selected != null) list.moveHighlight(delta)
        list.highlightedOption?.let { choose(it.value, context) }
        return true
    }
}

/**
 * The option list of a select or native select. It takes every key while open: Up and Down move
 * the highlight, Home and End jump to the ends, and Enter or Space choose the highlighted option.
 *
 * @property owner the select that opened the list
 */
class SelectPopover(override val owner: SelectWidget) : Popover {

    /**
     * The height the list was last drawn at.
     */
    private var viewHeight: Int = OptionList.MAX_HEIGHT

    /**
     * Places the list below the select, at least as wide as it.
     *
     * @param window the window area
     * @param measurer the text measurer
     * @return the list's area
     */
    override fun area(window: Rect, measurer: TextMeasurer): Rect {
        val width = maxOf(owner.bounds.width, owner.list.preferredWidth(measurer))
        val height = owner.list.contentHeight.coerceAtMost(OptionList.MAX_HEIGHT)
        viewHeight = height
        return Popovers.below(owner.bounds, width, height, window)
    }

    /**
     * Draws the list with a check mark on the selected option.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the popover
     * @param area the list's area
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, area: Rect, mouseX: Int, mouseY: Int) {
        drawPopoverFrame(ui, area)
        owner.list.render(ui, area, listOfNotNull(owner.selected), mouseX, mouseY, null)
    }

    /**
     * Chooses the option under a left click and closes the list.
     *
     * @param context the screen showing the popover
     * @param area the list's area
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     */
    override fun mouseClicked(context: UiContext, area: Rect, x: Double, y: Double, button: Int) {
        val row = owner.list.rows.getOrNull(owner.list.rowAt(area, y)) as? OptionList.Item ?: return
        if (!row.option.enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return
        owner.choose(row.option.value, context)
        context.closePopover()
    }

    /**
     * Scrolls the list.
     *
     * @param area the list's area
     * @param amount the scroll amount; positive scrolls up
     */
    override fun mouseScrolled(area: Rect, amount: Double) {
        owner.list.scrollBy(-(amount * UiMetrics.SCROLL_STEP).toInt(), area.height)
    }

    /**
     * Moves the highlight or chooses the highlighted option; leaves Escape and Tab to the screen.
     *
     * @param context the screen showing the popover
     * @param event the key event
     * @return `false` for Escape and Tab, `true` for every other key
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        val list = owner.list
        when (event.key()) {
            GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_TAB -> return false
            GLFW.GLFW_KEY_UP -> list.moveHighlight(-1)
            GLFW.GLFW_KEY_DOWN -> list.moveHighlight(1)
            GLFW.GLFW_KEY_HOME -> list.highlightEnd(last = false)
            GLFW.GLFW_KEY_END -> list.highlightEnd(last = true)
            else -> if (isActivation(event)) {
                list.highlightedOption?.let { owner.choose(it.value, context) }
                context.closePopover()
            }
        }
        list.revealHighlight(viewHeight)
        return true
    }

    /**
     * Swallows typed characters while the list is open.
     *
     * @param context the screen showing the popover
     * @param event the character event
     * @return `true`
     */
    override fun charTyped(context: UiContext, event: CharacterEvent): Boolean = true
}

/**
 * A text field that filters an option list as the player types, for one option or several shown
 * as chips before the typed text. Up and Down move the highlight in the list, Enter chooses the
 * highlighted option, and Backspace in an empty field removes the last chip.
 *
 * @param id the id of the widget
 * @param groups the option groups
 * @param selected the values of the selected options
 * @property multiple whether several options can be selected
 * @property placeholder the hint shown while the field is empty, as component JSON
 * @property emptyText the text shown when no option matches, as component JSON
 * @property showClear whether a button clears the selection
 * @property required whether having no selection is invalid
 * @param plainText returns the plain text of an option label, which the query is matched against
 * @property notifySearch whether the typed query is reported to the server, which then filters
 *           the options itself; the combobox filters them otherwise
 */
class ComboboxWidget(
    id: String,
    groups: List<SelectGroup>,
    selected: List<String>,
    val multiple: Boolean,
    var placeholder: String,
    val emptyText: String,
    val showClear: Boolean,
    val required: Boolean,
    plainText: (String) -> String = PlainText::of,
    val notifySearch: Boolean = false,
) : Widget(id) {

    /**
     * The options, filtered by the typed query unless the server filters them.
     */
    val list: OptionList = OptionList(groups, plainText, filters = !notifySearch)

    /**
     * The typed query and its cursor.
     */
    val edit: TextEditState = TextEditState()

    /**
     * The values of the selected options, in the order they were selected.
     */
    private val chosen: MutableList<String> = selected.toMutableList()

    /**
     * The labels of the selected options, kept for options the server removes.
     */
    private val chosenLabels: MutableMap<String, String> = selected.associateWith { list.label(it) ?: it }.toMutableMap()

    /**
     * The areas of the chips' remove buttons, set by the last frame.
     */
    private var chipRemoves: List<Pair<String, Rect>> = emptyList()

    /**
     * The area of the clear button, set by the last frame, or `null` if it is hidden.
     */
    private var clearArea: Rect? = null

    /**
     * The number of chip rows the last layout was sized for.
     */
    private var chipRows: Int = 1

    /**
     * The measurer of the last layout.
     */
    private var measurer: TextMeasurer? = null

    /**
     * The x position the typed query started at in the last frame, or `null` before the first
     * frame.
     */
    private var queryLeft: Int? = null

    /**
     * Counts consecutive clicks on the query to tell single, double and triple clicks apart.
     */
    private val clicks: ClickCounter = ClickCounter()

    /**
     * Whether the combobox can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * The values of the selected options joined by commas, in the order they were selected.
     */
    override val inputValue: String get() = chosen.joinToString(",")

    /**
     * The values of the selected options, in the order they were selected.
     */
    val selectedValues: List<String> get() = chosen.toList()

    /**
     * Whether the combobox shows itself as invalid: while the server marks it invalid, or once touched while required and empty.
     */
    override val showsInvalid: Boolean get() = serverInvalid || touched && required && chosen.isEmpty()

    /**
     * Returns the width of one chip.
     *
     * @param measurer the text measurer
     * @param value the value of the selected option
     * @return the chip width
     */
    private fun chipWidth(measurer: TextMeasurer, value: String): Int =
        measurer.width(chosenLabels[value] ?: value) + 2 * CHIP_PADDING + CHIP_REMOVE

    /**
     * Places the chips in rows within a width, followed by room for the typed text.
     *
     * @param measurer the text measurer
     * @param width the inner width of the field
     * @return the chip areas relative to the field's inner top left, and the start of the text
     */
    private fun placeChips(measurer: TextMeasurer, width: Int): Pair<List<Pair<String, Rect>>, Pair<Int, Int>> {
        var x = 0
        var row = 0
        val placed = chosen.map { value ->
            val chipWidth = chipWidth(measurer, value)
            if (x > 0 && x + chipWidth > width) {
                x = 0
                row++
            }
            (value to Rect(x, row * (CHIP_HEIGHT + CHIP_GAP), chipWidth, CHIP_HEIGHT)).also { x += chipWidth + CHIP_GAP }
        }
        if (x > 0 && x + MIN_TEXT_WIDTH > width) {
            x = 0
            row++
        }
        return placed to (x to row)
    }

    /**
     * The inner width of the field, from the last layout or the default width.
     */
    private val innerWidth: Int
        get() = ((if (bounds.width > 0) bounds.width else DEFAULT_WIDTH) - 2 * UiMetrics.WIDGET_PADDING - UiMetrics.INLINE_ICON - UiMetrics.ICON_GAP).coerceAtLeast(1)

    /**
     * Returns the default width and a height that fits the chip rows.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        this.measurer = measurer
        chipRows = if (multiple) placeChips(measurer, innerWidth).second.second + 1 else 1
        val height = if (multiple && chipRows > 1) chipRows * (CHIP_HEIGHT + CHIP_GAP) - CHIP_GAP + 2 * CHIP_EDGE else UiMetrics.WIDGET_HEIGHT
        return Size(DEFAULT_WIDTH, maxOf(UiMetrics.WIDGET_HEIGHT, height))
    }

    /**
     * Draws the field with its chips or selected label, the typed text or placeholder, the clear
     * button and a chevron. It asks for a new layout when its chips need another row count.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val focused = context.focusedWidget === this
        val fade: (Int) -> Int = { if (enabled) it else ui.disabled(it) }
        ui.fillRounded(bounds, inputFill(ui))
        ui.borderRounded(bounds, when {
            showsInvalid -> tokens.destructive
            focused -> tokens.ring
            else -> tokens.input
        })
        val side = UiMetrics.INLINE_ICON
        val left = bounds.x + UiMetrics.WIDGET_PADDING
        val chevron = Rect(bounds.right - UiMetrics.WIDGET_PADDING - side, bounds.y + (bounds.height - side) / 2, side, side)
        ui.icon("chevron-down", chevron, fade(tokens.mutedForeground))
        clearArea = if (showClear && chosen.isNotEmpty() && enabled) Rect(chevron.x - side - UiMetrics.ICON_GAP, chevron.y, side, side) else null
        clearArea?.let { ui.icon("x", it, tokens.mutedForeground) }
        val textRight = (clearArea ?: chevron).x - UiMetrics.ICON_GAP

        var textX = left
        var textY = bounds.y + (bounds.height - ui.lineHeight + 1) / 2
        if (multiple) {
            val (chips, textStart) = placeChips(ui, innerWidth)
            if (chips.isNotEmpty() && textStart.second + 1 != chipRows) context.requestLayout()
            val top = if (chipRows > 1) bounds.y + CHIP_EDGE else bounds.y + (bounds.height - CHIP_HEIGHT) / 2
            chipRemoves = chips.map { (value, rel) ->
                val chip = Rect(left + rel.x, top + rel.y, rel.width, rel.height)
                ui.fillRounded(chip, fade(tokens.muted), (tokens.radius - 1).coerceAtLeast(0))
                ui.text(chosenLabels[value] ?: value, chip.x + CHIP_PADDING, chip.y + (chip.height - ui.lineHeight + 1) / 2, fade(tokens.foreground))
                val remove = Rect(chip.right - CHIP_REMOVE, chip.y + (chip.height - CHIP_ICON) / 2, CHIP_ICON, CHIP_ICON)
                ui.icon("x", remove, fade(tokens.mutedForeground))
                value to remove
            }
            textX = left + textStart.first
            textY = top + textStart.second * (CHIP_HEIGHT + CHIP_GAP) + (CHIP_HEIGHT - ui.lineHeight + 1) / 2
        } else {
            chipRemoves = emptyList()
        }
        queryLeft = textX
        ui.clipped(Rect(textX, bounds.y, (textRight - textX).coerceAtLeast(0), bounds.height)) {
            when {
                edit.text.isNotEmpty() -> {
                    ui.plainText(edit.text, textX, textY, fade(tokens.foreground))
                    if (focused) ui.textSelection(edit.text, 0, edit.text.length, edit, textX, textY)
                }
                !multiple && chosen.isNotEmpty() -> ui.text(chosenLabels[chosen.first()] ?: "", textX, textY, fade(if (focused) tokens.mutedForeground else tokens.foreground))
                chosen.isEmpty() -> ui.text(placeholder, textX, textY, fade(tokens.mutedForeground))
            }
            if (focused && System.currentTimeMillis() / CURSOR_BLINK_MILLIS % 2 == 0L) {
                val cursorX = textX + ui.plainWidth(edit.text.substring(0, edit.cursor))
                ui.fill(Rect(cursorX, textY - 1, 1, ui.lineHeight + 1), tokens.foreground)
            }
        }
    }

    /**
     * Returns whether this combobox's list is open.
     *
     * @param context the screen showing the widget
     * @return whether the open popover belongs to this combobox
     */
    private fun isOpen(context: UiContext): Boolean = context.popover?.owner === this

    /**
     * Opens the option list if it is not open.
     *
     * @param context the screen showing the widget
     */
    fun open(context: UiContext) {
        if (isOpen(context)) return
        list.highlightValue(chosen.lastOrNull())
        context.openPopover(ComboboxPopover(this))
    }

    /**
     * Selects an option: in a multiple combobox it is added or removed and the list stays open,
     * in a single one it becomes the selection and the list closes. The typed query is cleared.
     *
     * @param value the value of the option
     * @param context the screen showing the widget
     */
    fun choose(value: String, context: UiContext) {
        val option = list.options.firstOrNull { it.value == value } ?: return
        if (!option.enabled) return
        if (multiple) {
            if (!chosen.remove(value)) chosen += value
        } else {
            chosen.clear()
            chosen += value
            context.closePopover()
        }
        chosenLabels[value] = option.label
        setQuery("", context)
        markChanged(context, immediate = true)
    }

    /**
     * Removes a selected option.
     *
     * @param value the value of the option
     * @param context the screen showing the widget
     */
    fun remove(value: String, context: UiContext) {
        if (chosen.remove(value)) markChanged(context, immediate = true)
    }

    /**
     * Clears the selection and the typed query.
     *
     * @param context the screen showing the widget
     */
    fun clear(context: UiContext) {
        setQuery("", context)
        if (chosen.isEmpty()) return
        chosen.clear()
        markChanged(context, immediate = true)
    }

    /**
     * Replaces the typed query, filters the list and reports the query if it changed.
     *
     * @param query the new query
     * @param context the screen showing the widget
     */
    private fun setQuery(query: String, context: UiContext) {
        if (edit.text != query) edit.text = query
        queryChanged(context)
    }

    /**
     * Filters the list by the typed query and reports a changed query if the combobox reports its
     * searches.
     *
     * @param context the screen showing the widget
     */
    private fun queryChanged(context: UiContext) {
        if (list.query == edit.text) return
        list.query = edit.text
        if (notifySearch) context.searchChanged(this, edit.text)
    }

    /**
     * Replaces the options, keeping selected options that are missing from the new groups in an
     * extra group at the end.
     *
     * @param groups the new option groups
     */
    fun replaceOptions(groups: List<SelectGroup>) {
        val present = groups.flatMap { group -> group.options.map { it.value } }.toSet()
        val kept = list.options.filter { it.value in chosen && it.value !in present }
        list.groups = if (kept.isEmpty()) groups else groups + SelectGroup(null, kept)
    }

    /**
     * Handles a click: a chip's remove button or the clear button act on the selection, anywhere
     * else the combobox takes the focus and opens its list. Such a click places the cursor in the
     * typed query, a double click selects the word under the mouse and a triple click the whole
     * query; with Shift, the click extends the selection.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this combobox
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (!enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true
        context.focus(this)
        chipRemoves.firstOrNull { it.second.grow(2).contains(x, y) }?.let {
            remove(it.first, context)
            return true
        }
        if (clearArea?.grow(2)?.contains(x, y) == true) {
            clear(context)
            return true
        }
        val measurer = measurer
        val left = queryLeft
        if (measurer == null || left == null) {
            edit.cursor = edit.text.length
        } else {
            val line = TextLines.Line(0, edit.text.length)
            val offset = (x - left).toInt()
            val position = TextLines.positionAt(edit.text, line, offset, measurer::plainWidth)
            val charIndex = TextLines.charIndexAt(edit.text, line, offset, measurer::plainWidth)
            applyTextClick(edit, edit.text, clicks, context, x, y, context.timeMillis, position, charIndex) { TextRange(0, edit.text.length) }
        }
        open(context)
        return true
    }

    /**
     * Whether a press in the combobox starts a drag that selects text in the typed query, which
     * it does while enabled.
     */
    override val draggable: Boolean get() = enabled

    /**
     * Extends the selection in the typed query to the mouse while the button stays held after a
     * press in the combobox.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     */
    override fun mouseDragged(context: UiContext, x: Double, y: Double) {
        val measurer = measurer ?: return
        val left = queryLeft ?: return
        val position = TextLines.positionAt(edit.text, TextLines.Line(0, edit.text.length), (x - left).toInt(), measurer::plainWidth)
        edit.moveCursorTo(position, extend = true)
    }

    /**
     * Moves the list highlight, chooses the highlighted option, edits the query with the editing
     * keys of [TextEditKeys], and removes the last chip with Backspace at the start of the field.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled) return false
        clicks.reset()
        when (TextEditKeys.handleClipboard(edit, event, context, lineBreak = " ")) {
            TextEditKeys.Result.IGNORED -> Unit
            TextEditKeys.Result.MOVED -> return true
            TextEditKeys.Result.CHANGED -> {
                queryChanged(context)
                open(context)
                return true
            }
        }
        when (event.key()) {
            GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_UP -> {
                if (!isOpen(context)) open(context) else list.moveHighlight(if (event.key() == GLFW.GLFW_KEY_DOWN) 1 else -1)
                list.revealHighlight(OptionList.MAX_HEIGHT)
            }

            GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if (!isOpen(context)) return false
                list.highlightedOption?.let { choose(it.value, context) }
            }

            else -> if (event.key() == GLFW.GLFW_KEY_BACKSPACE && multiple && chosen.isNotEmpty() && edit.cursor == 0 && !edit.hasSelection) {
                remove(chosen.last(), context)
            } else when (TextEditKeys.handle(edit, event)) {
                TextEditKeys.Result.IGNORED -> return false
                TextEditKeys.Result.MOVED -> Unit
                TextEditKeys.Result.CHANGED -> {
                    queryChanged(context)
                    if (event.key() == GLFW.GLFW_KEY_BACKSPACE) open(context)
                }
            }
        }
        return true
    }

    /**
     * Adds a typed character to the query, filters the list and opens it.
     *
     * @param context the screen showing the widget
     * @param event the character event
     * @return whether the character was handled
     */
    override fun charTyped(context: UiContext, event: CharacterEvent): Boolean {
        if (!enabled || !event.isAllowedChatCharacter) return false
        if (edit.insert(event.codepointAsString())) {
            queryChanged(context)
            open(context)
        }
        return true
    }

    /**
     * Sets the selection from the comma-separated values of known options.
     *
     * @param value the selected values
     */
    override fun applyValue(value: String) {
        val values = value.split(',').map { it.trim() }.filter { v -> v.isNotEmpty() && list.options.any { it.value == v } }.distinct()
        chosen.clear()
        chosen += if (multiple) values else values.take(1)
        chosen.forEach { v -> list.label(v)?.let { chosenLabels[v] = it } }
    }

    /**
     * Replaces the placeholder.
     *
     * @param json the new placeholder as component JSON
     */
    override fun applyText(json: String) {
        placeholder = json
    }

    /**
     * Holds the field and chip sizes.
     */
    private companion object {
        /**
         * The default width of a combobox.
         */
        const val DEFAULT_WIDTH: Int = 160

        /**
         * The height of a chip.
         */
        const val CHIP_HEIGHT: Int = 12

        /**
         * The space between two chips.
         */
        const val CHIP_GAP: Int = 3

        /**
         * The space above and below the chip rows of a field with several rows.
         */
        const val CHIP_EDGE: Int = 4

        /**
         * The horizontal space inside a chip before its label.
         */
        const val CHIP_PADDING: Int = 3

        /**
         * The width a chip keeps for its remove button.
         */
        const val CHIP_REMOVE: Int = 10

        /**
         * The side length of a chip's remove icon.
         */
        const val CHIP_ICON: Int = 7

        /**
         * The width kept for typed text after the chips.
         */
        const val MIN_TEXT_WIDTH: Int = 30

        /**
         * How long the cursor stays visible or hidden while blinking.
         */
        const val CURSOR_BLINK_MILLIS: Long = 500
    }
}

/**
 * The option list of a combobox. It leaves every key to the combobox, which filters and moves
 * the highlight.
 *
 * @property owner the combobox that opened the list
 */
class ComboboxPopover(override val owner: ComboboxWidget) : Popover {

    /**
     * Clicks on the combobox reach it, so that chips can be removed and the selection cleared
     * while the list is open.
     */
    override val passesOwnerClicks: Boolean get() = true

    /**
     * Places the list below the combobox, as wide as it.
     *
     * @param window the window area
     * @param measurer the text measurer
     * @return the list's area
     */
    override fun area(window: Rect, measurer: TextMeasurer): Rect {
        val height = if (owner.list.rows.isEmpty()) EMPTY_HEIGHT else owner.list.contentHeight.coerceAtMost(OptionList.MAX_HEIGHT)
        return Popovers.below(owner.bounds, owner.bounds.width, height, window)
    }

    /**
     * Draws the filtered list with check marks on the selected options, or the empty text.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the popover
     * @param area the list's area
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, area: Rect, mouseX: Int, mouseY: Int) {
        drawPopoverFrame(ui, area)
        owner.list.render(ui, area, owner.selectedValues, mouseX, mouseY, owner.emptyText)
    }

    /**
     * Chooses the option under a left click.
     *
     * @param context the screen showing the popover
     * @param area the list's area
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     */
    override fun mouseClicked(context: UiContext, area: Rect, x: Double, y: Double, button: Int) {
        val row = owner.list.rows.getOrNull(owner.list.rowAt(area, y)) as? OptionList.Item ?: return
        if (row.option.enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) owner.choose(row.option.value, context)
    }

    /**
     * Scrolls the list.
     *
     * @param area the list's area
     * @param amount the scroll amount; positive scrolls up
     */
    override fun mouseScrolled(area: Rect, amount: Double) {
        owner.list.scrollBy(-(amount * UiMetrics.SCROLL_STEP).toInt(), area.height)
    }

    /**
     * Leaves every key to the combobox.
     *
     * @param context the screen showing the popover
     * @param event the key event
     * @return `false`
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean = false

    /**
     * Holds the empty list size.
     */
    private companion object {
        /**
         * The height of the list while no option matches.
         */
        const val EMPTY_HEIGHT: Int = 24
    }
}

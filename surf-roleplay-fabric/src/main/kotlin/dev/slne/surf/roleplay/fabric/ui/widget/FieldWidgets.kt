package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.FieldTextKind
import dev.slne.surf.roleplay.protocol.screen.Orientation
import org.lwjgl.glfw.GLFW

/**
 * A form: a vertical stack whose single-line inputs click the submit button on Enter.
 *
 * @param id the id of the widget
 * @property submitId the id of the button that Enter clicks, or `null` for none
 */
class FormWidget(id: String, val submitId: String?) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = FORM_GAP
        crossAlign = Align.STRETCH
    }

    /**
     * Finds forms around widgets.
     */
    companion object {
        /**
         * The space between the parts of a form.
         */
        const val FORM_GAP: Int = 12

        /**
         * Finds the innermost form that contains a widget.
         *
         * @param root the root of the tree
         * @param widget the widget
         * @return the form, or `null` if the widget is in no form
         */
        fun around(root: Widget, widget: Widget): FormWidget? {
            var current: Widget = widget
            while (true) {
                val parent = WidgetTree.parentOf(root, current.id) ?: return null
                if (parent is FormWidget) return parent
                current = parent
            }
        }
    }
}

/**
 * A vertical stack of field parts: a field set, a field group or the texts of a horizontal
 * field.
 *
 * @param id the id of the widget
 * @param gap the space between two parts
 */
class FieldStackWidget(id: String, gap: Int) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        this.gap = gap
        crossAlign = Align.STRETCH
    }

    /**
     * Holds the gaps of the stacks.
     */
    companion object {
        /**
         * The space between the parts of a field set.
         */
        const val SET_GAP: Int = 10

        /**
         * The space between the fields of a group.
         */
        const val GROUP_GAP: Int = 10

        /**
         * The space between the texts of a field's content.
         */
        const val CONTENT_GAP: Int = 2
    }
}

/**
 * One form field. A vertical field stacks its parts; a horizontal one places them side by side,
 * centred. An invalid field draws its labels and titles in the destructive colour.
 *
 * @param id the id of the widget
 * @property orientation whether the parts are stacked or placed side by side
 */
class FieldWidget(id: String, val orientation: Orientation) :
    ContainerWidget(id, if (orientation == Orientation.VERTICAL) Axis.VERTICAL else Axis.HORIZONTAL) {
    init {
        if (orientation == Orientation.VERTICAL) {
            gap = VERTICAL_GAP
            crossAlign = Align.STRETCH
        } else {
            gap = HORIZONTAL_GAP
            crossAlign = Align.CENTER
        }
    }

    /**
     * Whether the field is invalid: while the server marks it invalid, or while one of its inputs
     * shows itself as invalid.
     */
    val invalid: Boolean
        get() {
            if (serverInvalid) return true
            var any = false
            WidgetTree.visit(this) { if (it !== this && it !is FieldTextWidget && it.showsInvalid) any = true }
            return any
        }

    /**
     * Draws the parts, with the texts of an invalid field in the destructive colour.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val invalid = invalid
        WidgetTree.visit(this) { if (it is FieldTextWidget) it.fieldInvalid = invalid }
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Holds the field gaps.
     */
    private companion object {
        /**
         * The space between the parts of a vertical field.
         */
        const val VERTICAL_GAP: Int = 4

        /**
         * The space between the parts of a horizontal field.
         */
        const val HORIZONTAL_GAP: Int = 8
    }
}

/**
 * A text of a form: a legend, label, title, description or error. A label with a target focuses
 * the target when clicked. An error is not drawn while empty.
 *
 * @param id the id of the widget
 * @property kind the kind of text
 * @property text the text as component JSON
 * @property forId the id of the widget a click on a label focuses, or `null` for none
 */
class FieldTextWidget(id: String, val kind: FieldTextKind, var text: String, val forId: String?) : Widget(id) {

    /**
     * Whether the field around the text is invalid, set by the field before drawing.
     */
    var fieldInvalid: Boolean = false

    /**
     * Whether the text is drawn: every text but an empty error.
     */
    private val shown: Boolean get() = kind != FieldTextKind.ERROR || PlainText.of(text).isNotEmpty()

    /**
     * Returns the size of the text on one line, or nothing for an empty error.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        if (shown) Size(measurer.width(text), measurer.lineHeight + if (kind == FieldTextKind.LEGEND) LEGEND_SPACE else 0) else Size.ZERO

    /**
     * Draws the text in its kind's colour: muted descriptions, destructive errors, and labels and
     * titles in the destructive colour while the field is invalid.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        if (!shown) return
        val tokens = ui.tokens
        val color = when (kind) {
            FieldTextKind.DESCRIPTION -> tokens.mutedForeground
            FieldTextKind.ERROR -> tokens.destructive
            FieldTextKind.LABEL, FieldTextKind.TITLE -> if (fieldInvalid) tokens.destructive else tokens.foreground
            FieldTextKind.LEGEND, FieldTextKind.LEGEND_LABEL -> tokens.foreground
        }
        ui.text(text, bounds.x, bounds.y + (bounds.height - ui.lineHeight + 1) / 2, color)
    }

    /**
     * Passes a left click on a label with a target to the target.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on a label with a target
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        val target = forId ?: return false
        if (!isOver(x, y)) return false
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) context.widget(target)?.labelClicked(context)
        return true
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
     * Holds the legend spacing.
     */
    private companion object {
        /**
         * The extra space below a legend.
         */
        const val LEGEND_SPACE: Int = 3
    }
}

/**
 * A line between fields, optionally with a text in its middle.
 *
 * @param id the id of the widget
 * @property text the text as component JSON, or `null` for a plain line
 */
class FieldSeparatorWidget(id: String, var text: String?) : Widget(id) {

    /**
     * Returns the height of a line, or of the text if it has one.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        Size(text?.let { measurer.width(it) + 2 * TEXT_GAP } ?: 0, if (text != null) measurer.lineHeight else LINE_HEIGHT)

    /**
     * Draws the line across the widget, broken around the text.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val lineY = bounds.y + bounds.height / 2
        val label = text
        if (label == null) {
            ui.fill(Rect(bounds.x, lineY, bounds.width, 1), tokens.border)
            return
        }
        val textWidth = ui.width(label)
        val textX = bounds.x + (bounds.width - textWidth) / 2
        ui.fill(Rect(bounds.x, lineY, (textX - TEXT_GAP - bounds.x).coerceAtLeast(0), 1), tokens.border)
        ui.fill(Rect(textX + textWidth + TEXT_GAP, lineY, (bounds.right - textX - textWidth - TEXT_GAP).coerceAtLeast(0), 1), tokens.border)
        ui.text(label, textX, bounds.y + (bounds.height - ui.lineHeight + 1) / 2, tokens.mutedForeground)
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
     * Holds the separator sizes.
     */
    private companion object {
        /**
         * The height of a separator without text.
         */
        const val LINE_HEIGHT: Int = 5

        /**
         * The space between the text and the line.
         */
        const val TEXT_GAP: Int = 6
    }
}

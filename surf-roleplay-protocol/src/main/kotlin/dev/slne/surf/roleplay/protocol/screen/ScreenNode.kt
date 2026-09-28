package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * A node of a generic screen tree: a container or a widget.
 *
 * Every node has an id that is unique within its screen, and a width and height sizing that its
 * parent container uses for layout. Texts are text components in their JSON form.
 */
@Serializable
sealed interface ScreenNode {
    /**
     * The id of this node, unique within its screen.
     */
    val id: String

    /**
     * How wide this node is laid out.
     */
    val width: Sizing

    /**
     * How tall this node is laid out.
     */
    val height: Sizing
}

/**
 * A node that lays out child nodes along one axis.
 */
sealed interface ContainerNode : ScreenNode {
    /**
     * The child nodes, in layout order.
     */
    val children: List<ScreenNode>

    /**
     * Returns a copy of this container with other children.
     *
     * @param children the new children
     * @return the copy
     */
    fun withChildren(children: List<ScreenNode>): ContainerNode
}

/**
 * How a node is sized along one axis.
 *
 * @property mode the sizing mode
 * @property value the size in GUI pixels for [SizeMode.FIXED], the grow weight for
 *           [SizeMode.GROW], and unused for [SizeMode.FIT]
 */
@Serializable
data class Sizing(
    @ProtoNumber(1) val mode: SizeMode = SizeMode.FIT,
    @ProtoNumber(2) val value: Int = 0,
) {
    /**
     * Creates the common sizings.
     */
    companion object {
        /**
         * The sizing that fits the node's content.
         */
        val FIT: Sizing = Sizing()

        /**
         * Creates a fixed sizing.
         *
         * @param pixels the size in GUI pixels
         * @return the sizing
         */
        fun fixed(pixels: Int): Sizing = Sizing(SizeMode.FIXED, pixels)

        /**
         * Creates a sizing that takes a share of the remaining space.
         *
         * @param weight the share relative to the weights of the node's growing siblings
         * @return the sizing
         */
        fun grow(weight: Int = 1): Sizing = Sizing(SizeMode.GROW, weight)
    }
}

/**
 * The ways a node can be sized along one axis.
 */
@Serializable
enum class SizeMode {
    /**
     * As large as the node's content needs.
     */
    @ProtoNumber(0)
    FIT,

    /**
     * A fixed number of GUI pixels.
     */
    @ProtoNumber(1)
    FIXED,

    /**
     * A weighted share of the space the container has left after its other children.
     */
    @ProtoNumber(2)
    GROW,
}

/**
 * How children are placed along an axis of a container.
 */
@Serializable
enum class Align {
    /**
     * At the start of the axis.
     */
    @ProtoNumber(0)
    START,

    /**
     * Centered on the axis.
     */
    @ProtoNumber(1)
    CENTER,

    /**
     * At the end of the axis.
     */
    @ProtoNumber(2)
    END,

    /**
     * Stretched over the whole cross axis. On the main axis it behaves like [START].
     */
    @ProtoNumber(3)
    STRETCH,
}

/**
 * Space inside a container's edges, in GUI pixels.
 *
 * @property top the space at the top
 * @property right the space at the right
 * @property bottom the space at the bottom
 * @property left the space at the left
 */
@Serializable
data class Insets(
    @ProtoNumber(1) val top: Int = 0,
    @ProtoNumber(2) val right: Int = 0,
    @ProtoNumber(3) val bottom: Int = 0,
    @ProtoNumber(4) val left: Int = 0,
) {
    /**
     * Creates common insets.
     */
    companion object {
        /**
         * No space at any edge.
         */
        val NONE: Insets = Insets()

        /**
         * Creates the same space at every edge.
         *
         * @param pixels the space in GUI pixels
         * @return the insets
         */
        fun all(pixels: Int): Insets = Insets(pixels, pixels, pixels, pixels)
    }
}

/**
 * A container that lays out its children from left to right.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the child nodes, from left to right
 * @property gap the space between two children, in GUI pixels
 * @property padding the space inside the container's edges
 * @property mainAlign how the children are placed horizontally
 * @property crossAlign how the children are placed vertically
 */
@Serializable
@SerialName("row")
data class RowNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val gap: Int = 0,
    @ProtoNumber(6) val padding: Insets = Insets.NONE,
    @ProtoNumber(7) val mainAlign: Align = Align.START,
    @ProtoNumber(8) val crossAlign: Align = Align.START,
) : ContainerNode {
    /**
     * Returns a copy of this row with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): RowNode = copy(children = children)
}

/**
 * A container that lays out its children from top to bottom.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the child nodes, from top to bottom
 * @property gap the space between two children, in GUI pixels
 * @property padding the space inside the container's edges
 * @property mainAlign how the children are placed vertically
 * @property crossAlign how the children are placed horizontally
 */
@Serializable
@SerialName("column")
data class ColumnNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val gap: Int = 0,
    @ProtoNumber(6) val padding: Insets = Insets.NONE,
    @ProtoNumber(7) val mainAlign: Align = Align.START,
    @ProtoNumber(8) val crossAlign: Align = Align.START,
) : ContainerNode {
    /**
     * Returns a copy of this column with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ColumnNode = copy(children = children)
}

/**
 * A scrollable column of child nodes. Its content can be taller than the list itself.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the child nodes, from top to bottom
 * @property gap the space between two children, in GUI pixels
 */
@Serializable
@SerialName("scroll_list")
data class ScrollListNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val gap: Int = 0,
) : ContainerNode {
    /**
     * Returns a copy of this list with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ScrollListNode = copy(children = children)
}

/**
 * A piece of styled text.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 * @property icon the name of a Lucide icon drawn before the text, or `null` for none
 */
@Serializable
@SerialName("label")
data class LabelNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
) : ScreenNode

/**
 * A button that sends a widget action with the screen's input values when clicked.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the caption as component JSON
 * @property enabled whether the button can be clicked
 * @property submitsInput whether a click requires every input of the screen to be valid; a button
 *           that does not submit input, such as a back button, is accepted with invalid inputs
 * @property icon the name of a Lucide icon drawn before the caption, or `null` for none
 * @property variant the look of the button
 * @property size the size of the button
 */
@Serializable
@SerialName("button")
data class ButtonNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val enabled: Boolean = true,
    @ProtoNumber(6) val submitsInput: Boolean = true,
    @ProtoNumber(7) val icon: String? = null,
    @ProtoNumber(8) val variant: ButtonVariant = ButtonVariant.DEFAULT,
    @ProtoNumber(9) val size: ButtonSize = ButtonSize.DEFAULT,
) : ScreenNode

/**
 * A single-line text field.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property value the current text
 * @property placeholder the hint shown while the field is empty, as component JSON
 * @property maxLength the maximum number of characters, or `null` for no limit
 * @property required whether an empty value is invalid
 * @property enabled whether the player can edit the field
 * @property icon the name of a Lucide icon drawn at the start of the field, or `null` for none
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("text_input")
data class TextInputNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val value: String = "",
    @ProtoNumber(5) val placeholder: String = "",
    @ProtoNumber(6) val maxLength: Int? = null,
    @ProtoNumber(7) val required: Boolean = false,
    @ProtoNumber(8) val enabled: Boolean = true,
    @ProtoNumber(9) val icon: String? = null,
    @ProtoNumber(10) val notifyChange: Boolean = false,
) : ScreenNode

/**
 * A field for a whole number.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property value the current number, or `null` if the field is empty
 * @property min the smallest allowed number, or `null` for no lower bound
 * @property max the largest allowed number, or `null` for no upper bound
 * @property required whether an empty value is invalid
 * @property enabled whether the player can edit the field
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("number_input")
data class NumberInputNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val value: Long? = null,
    @ProtoNumber(5) val min: Long? = null,
    @ProtoNumber(6) val max: Long? = null,
    @ProtoNumber(7) val required: Boolean = false,
    @ProtoNumber(8) val enabled: Boolean = true,
    @ProtoNumber(9) val notifyChange: Boolean = false,
) : ScreenNode

/**
 * A box that is either checked or not, with a label next to it.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property label the label as component JSON
 * @property checked whether the box is checked
 * @property enabled whether the player can toggle the box
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("checkbox")
data class CheckboxNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val label: String = "",
    @ProtoNumber(5) val checked: Boolean = false,
    @ProtoNumber(6) val enabled: Boolean = true,
    @ProtoNumber(7) val notifyChange: Boolean = false,
) : ScreenNode

/**
 * A choice of one option from a list.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property options the options, in display order
 * @property selected the value of the selected option, or `null` if none is selected
 * @property required whether having no selection is invalid
 * @property enabled whether the player can change the selection
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("dropdown")
data class DropdownNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val options: List<DropdownOption> = emptyList(),
    @ProtoNumber(5) val selected: String? = null,
    @ProtoNumber(6) val required: Boolean = false,
    @ProtoNumber(7) val enabled: Boolean = true,
    @ProtoNumber(8) val notifyChange: Boolean = false,
) : ScreenNode

/**
 * One option of a [DropdownNode].
 *
 * @property value the value submitted when this option is selected
 * @property label the label shown for this option, as component JSON
 */
@Serializable
data class DropdownOption(
    @ProtoNumber(1) val value: String,
    @ProtoNumber(2) val label: String,
)

/**
 * A texture drawn over the node's whole area.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property texture the identifier of the texture, such as `surf-roleplay:textures/gui/logo.png`
 */
@Serializable
@SerialName("image")
data class ImageNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val texture: String = "",
) : ScreenNode

/**
 * A bar that shows how far something has progressed.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property progress the filled fraction, from `0` to `1`
 * @property label the text drawn over the bar as component JSON, or `null` for none
 */
@Serializable
@SerialName("progress")
data class ProgressNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val progress: Float = 0f,
    @ProtoNumber(5) val label: String? = null,
) : ScreenNode

/**
 * A Lucide icon, drawn square and tinted with a theme token.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property icon the Lucide name of the icon, such as `trash-2`
 * @property size the side length the icon fits into when the node fits its content, in GUI pixels
 * @property color the theme token the icon is tinted with
 */
@Serializable
@SerialName("icon")
data class IconNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val icon: String = "",
    @ProtoNumber(5) val size: Int = 16,
    @ProtoNumber(6) val color: IconColor = IconColor.FOREGROUND,
) : ScreenNode

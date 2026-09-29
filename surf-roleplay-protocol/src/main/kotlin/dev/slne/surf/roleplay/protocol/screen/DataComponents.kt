package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * A table: its header, body and footer rows with columns that line up across all rows, and an
 * optional caption below.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the caption, header, body and footer
 */
@Serializable
@SerialName("table")
data class TableNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TableNode = copy(children = children)
}

/**
 * The caption of a table, a muted text below it.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 */
@Serializable
@SerialName("table_caption")
data class TableCaptionNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
) : ScreenNode

/**
 * The part of a table a group of rows belongs to.
 */
@Serializable
enum class TableSection {
    /**
     * The header rows, above the body.
     */
    @ProtoNumber(0)
    HEADER,

    /**
     * The body rows.
     */
    @ProtoNumber(1)
    BODY,

    /**
     * The footer rows, below the body, on a muted background.
     */
    @ProtoNumber(2)
    FOOTER,
}

/**
 * A group of rows of a table: its header, body or footer.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the rows
 * @property section which part of the table the rows are
 */
@Serializable
@SerialName("table_section")
data class TableSectionNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val section: TableSection = TableSection.BODY,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TableSectionNode = copy(children = children)
}

/**
 * A row of a table: its cells, one per column, with a border below and a highlight on hover.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the cells
 * @property selected whether the row is drawn as selected
 */
@Serializable
@SerialName("table_row")
data class TableRowNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val selected: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TableRowNode = copy(children = children)
}

/**
 * A cell of a table row: its content, stacked. A head cell is a column heading in a medium weight
 * and a fixed height.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, stacked
 * @property head whether the cell is a column heading
 * @property align how the content is placed across the cell's width
 */
@Serializable
@SerialName("table_cell")
data class TableCellNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val head: Boolean = false,
    @ProtoNumber(6) val align: Align = Align.START,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TableCellNode = copy(children = children)
}

/**
 * A data table: a table of rows that the player sorts by a column, filters by a text, pages
 * through and selects, all in the mod. Its view is its input value, a [DataTableView] as JSON.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the columns, then the rows
 * @property pageSize the number of rows on a page, or `0` for one page of every row
 * @property selectable whether rows are selected with a checkbox column
 * @property filterColumn the key of the column the filter searches, or `null` for no filter
 * @property filterPlaceholder the placeholder of the filter input as component JSON
 * @property value the view the table starts with, as JSON; empty for the default view
 * @property notifyChange whether the mod reports every change of the view
 */
@Serializable
@SerialName("data_table")
data class DataTableNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val pageSize: Int = 10,
    @ProtoNumber(6) val selectable: Boolean = false,
    @ProtoNumber(7) val filterColumn: String? = null,
    @ProtoNumber(8) val filterPlaceholder: String = "",
    @ProtoNumber(9) val value: String = "",
    @ProtoNumber(10) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): DataTableNode = copy(children = children)
}

/**
 * A column of a data table: its key, its heading and whether the rows can be sorted by it.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property key the key of the column, unique within its table
 * @property header the heading as component JSON
 * @property sortable whether a click on the heading sorts by the column
 * @property align how the heading and the cells are placed across the column
 */
@Serializable
@SerialName("data_table_column")
data class DataTableColumnNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val key: String = "",
    @ProtoNumber(5) val header: String = "",
    @ProtoNumber(6) val sortable: Boolean = false,
    @ProtoNumber(7) val align: Align = Align.START,
) : ScreenNode

/**
 * A row of a data table: one cell per column, in the order of the columns.
 *
 * @property id the id of this node, also the id the view's selection names the row by
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the cells
 * @property selectable whether the row can be selected
 */
@Serializable
@SerialName("data_table_row")
data class DataTableRowNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val selectable: Boolean = true,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): DataTableRowNode = copy(children = children)
}

/**
 * A cell of a data table row: its content, stacked, and the key it sorts and filters by. Keys
 * that are both numbers compare as numbers, others as texts ignoring case.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, stacked
 * @property sortKey the key the cell sorts and filters by
 */
@Serializable
@SerialName("data_table_cell")
data class DataTableCellNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val sortKey: String = "",
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): DataTableCellNode = copy(children = children)
}

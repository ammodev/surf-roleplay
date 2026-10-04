package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.RowElement
import dev.slne.surf.roleplay.api.client.common.screen.ScrollListElement
import dev.slne.surf.roleplay.api.client.common.screen.Spacing

/**
 * Adds a row that lays out its children from left to right.
 *
 * @param width how wide the row is laid out
 * @param height how tall the row is laid out
 * @param gap the space between two children
 * @param padding the space inside the row's edges
 * @param mainAlign how the children are placed horizontally
 * @param crossAlign how the children are placed vertically
 * @param id the id of the row, or `null` for a generated one
 * @param children the builder of the children
 * @return the row
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Row(
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    gap: Int = 0,
    padding: Spacing = Spacing.NONE,
    mainAlign: Alignment = Alignment.START,
    crossAlign: Alignment = Alignment.START,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): RowElement {
    val elementId = nextId(id)
    return add(RowElement(elementId, this.children(children), width, height, gap, padding, mainAlign, crossAlign))
}

/**
 * Adds a column that lays out its children from top to bottom.
 *
 * @param width how wide the column is laid out
 * @param height how tall the column is laid out
 * @param gap the space between two children
 * @param padding the space inside the column's edges
 * @param mainAlign how the children are placed vertically
 * @param crossAlign how the children are placed horizontally
 * @param id the id of the column, or `null` for a generated one
 * @param children the builder of the children
 * @return the column
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Column(
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    gap: Int = 0,
    padding: Spacing = Spacing.NONE,
    mainAlign: Alignment = Alignment.START,
    crossAlign: Alignment = Alignment.START,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): ColumnElement {
    val elementId = nextId(id)
    return add(ColumnElement(elementId, this.children(children), width, height, gap, padding, mainAlign, crossAlign))
}

/**
 * Adds a scrollable column of children, stretched across the list's width.
 *
 * @param width how wide the list is laid out
 * @param height how tall the list is laid out
 * @param gap the space between two children
 * @param id the id of the list, or `null` for a generated one
 * @param children the builder of the children
 * @return the list
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ScrollList(
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    gap: Int = 0,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): ScrollListElement {
    val elementId = nextId(id)
    return add(ScrollListElement(elementId, this.children(children), width, height, gap))
}

package dev.slne.surf.roleplay.paper.storybook.stories

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.EmptyMediaVariant
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Empty
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyMedia
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Icon
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Muted
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Pagination
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationEllipsis
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationLink
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationNext
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationPrevious
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.paper.storybook.IconPaging
import dev.slne.surf.roleplay.paper.storybook.LucideCatalog
import dev.slne.surf.roleplay.paper.storybook.Story
import dev.slne.surf.roleplay.paper.storybook.StoryCategory
import net.kyori.adventure.text.Component

/**
 * The search, category filter and page of the icon gallery. Writing a property re-renders the
 * page that holds it.
 */
interface IconGalleryState {
    /**
     * The text the icon names and aliases are searched for; empty matches every icon.
     */
    var iconQuery: String

    /**
     * The Lucide category the icons must belong to, or `null` for any.
     */
    var iconCategory: String?

    /**
     * The zero-based page of the matching icons that is shown.
     */
    var iconPage: Int
}

/**
 * The number of icons in one row of the gallery grid.
 */
private const val ICONS_PER_ROW = 8

/**
 * The value of the category select that stands for every category.
 */
private const val ALL_CATEGORIES = "alle"

/**
 * Creates the story of the icon gallery: a search input, a category select, a grid of the
 * matching icons with their names, one page at a time, and a pagination; an empty state when
 * nothing matches.
 *
 * @param catalog the icons the gallery lists
 * @param state the search, filter and page the gallery shows and changes
 * @return the story
 */
fun iconGalleryStory(catalog: LucideCatalog, state: IconGalleryState): Story {
    val categories = listOf(SelectChoice(ALL_CATEGORIES, Component.text("Alle Kategorien"))) +
        catalog.categories.map { SelectChoice(it, Component.text(it)) }
    return Story("icons", "Symbole", StoryCategory.ICONS) { iconGallery(catalog, state, categories) }
}

/**
 * Adds the gallery's controls, grid and pagination.
 *
 * @param catalog the icons the gallery lists
 * @param state the search, filter and page
 * @param categories the options of the category select
 */
private fun ComponentScope.iconGallery(catalog: LucideCatalog, state: IconGalleryState, categories: List<SelectChoice>) {
    val matches = catalog.search(state.iconQuery, state.iconCategory)
    val pageCount = IconPaging.pageCount(matches.size)
    val page = state.iconPage.coerceIn(0, pageCount - 1)
    Row(width = ElementSize.grow(), gap = 6, crossAlign = Alignment.CENTER) {
        Input(value = state.iconQuery, placeholder = Component.text("Symbole suchen …"), maxLength = 64, width = ElementSize.grow(), icon = "search", id = "icon_search") { change ->
            state.iconQuery = change.value
            state.iconPage = 0
        }
        Select(categories, selected = state.iconCategory ?: ALL_CATEGORIES, width = ElementSize.fixed(140), id = "icon_category") { change ->
            state.iconCategory = change.value.takeIf { it != ALL_CATEGORIES }
            state.iconPage = 0
        }
    }
    if (matches.isEmpty()) {
        Empty(outline = true) {
            EmptyHeader {
                EmptyMedia(EmptyMediaVariant.ICON, icon = "search-x")
                EmptyTitle("Keine Symbole gefunden")
                EmptyDescription("Ändere die Suche oder wähle eine andere Kategorie.")
            }
        }
        return
    }
    Muted("${matches.size} Symbole, Seite ${page + 1} von $pageCount")
    Column(gap = 6) {
        IconPaging.page(matches, page).chunked(ICONS_PER_ROW).forEach { row ->
            Row(gap = 4) {
                row.forEach { icon ->
                    Column(width = ElementSize.fixed(64), gap = 2, crossAlign = Alignment.CENTER) {
                        Icon(icon.name)
                        Muted(icon.name, maxLines = 1, align = Alignment.CENTER, width = ElementSize.grow())
                    }
                }
            }
        }
    }
    if (pageCount > 1) iconPagination(state, page, pageCount)
}

/**
 * Adds the pagination of the gallery: previous, the pages around the shown one with gaps, and
 * next.
 *
 * @param state the state whose page the links change
 * @param page the shown page, zero-based
 * @param pageCount the number of pages
 */
private fun ComponentScope.iconPagination(state: IconGalleryState, page: Int, pageCount: Int) {
    Pagination {
        PaginationContent {
            PaginationItem { PaginationPrevious(enabled = page > 0, id = "icon_previous") { state.iconPage = page - 1 } }
            pageWindow(page, pageCount).forEach { index ->
                PaginationItem {
                    if (index == null) {
                        PaginationEllipsis()
                    } else {
                        PaginationLink("${index + 1}", active = index == page, id = "icon_page_$index") { state.iconPage = index }
                    }
                }
            }
            PaginationItem { PaginationNext(enabled = page < pageCount - 1, id = "icon_next") { state.iconPage = page + 1 } }
        }
    }
}

/**
 * Returns the pages a pagination links to: the first, the last, and the pages next to the
 * current one, in order, with `null` for every gap between them.
 *
 * @param current the current page, zero-based
 * @param count the number of pages, at least one
 * @return the zero-based pages and gaps
 */
internal fun pageWindow(current: Int, count: Int): List<Int?> {
    val pages = (listOf(0, count - 1) + (current - 1..current + 1)).filter { it in 0 until count }.distinct().sorted()
    val window = mutableListOf<Int?>()
    pages.forEachIndexed { index, page ->
        if (index > 0 && page - pages[index - 1] > 1) window += null
        window += page
    }
    return window
}

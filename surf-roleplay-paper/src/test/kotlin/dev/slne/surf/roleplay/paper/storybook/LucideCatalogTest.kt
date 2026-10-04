package dev.slne.surf.roleplay.paper.storybook

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the Lucide icon catalog and its paging.
 */
class LucideCatalogTest {

    /**
     * An index with three icons and one alias of the second icon.
     */
    private val json = """
        {"version":"1.48.0","cell":64,"columns":64,"width":4096,"height":64,"icons":{
        "arrow-up":{"x":0,"y":0,"categories":["arrows"]},
        "trash":{"x":64,"y":0,"categories":["tools","text"]},
        "trash-2":{"x":64,"y":0,"alias":"trash"},
        "user":{"x":128,"y":0,"categories":["people"]}}}
    """.trimIndent()

    /**
     * Aliases are folded into their icon and not listed as icons.
     */
    @Test
    fun `parse folds aliases into their icon`() {
        val catalog = LucideCatalog.parse(json)
        assertEquals(listOf("arrow-up", "trash", "user"), catalog.icons.map { it.name })
        assertEquals(listOf("trash-2"), catalog.icons.single { it.name == "trash" }.aliases)
        assertEquals(listOf("arrows", "people", "text", "tools"), catalog.categories)
    }

    /**
     * Search matches names and aliases ignoring case, and an empty query matches everything.
     */
    @Test
    fun `search matches names and aliases case-insensitively`() {
        val catalog = LucideCatalog.parse(json)
        assertEquals(listOf("trash"), catalog.search("TRASH-2", null).map { it.name })
        assertEquals(listOf("arrow-up"), catalog.search("Arrow", null).map { it.name })
        assertEquals(3, catalog.search("", null).size)
    }

    /**
     * A category filter narrows the result.
     */
    @Test
    fun `search filters by category`() {
        val catalog = LucideCatalog.parse(json)
        assertEquals(listOf("trash"), catalog.search("", "text").map { it.name })
        assertTrue(catalog.search("user", "arrows").isEmpty())
    }

    /**
     * Page counts are at least one and round up.
     */
    @Test
    fun `page count rounds up`() {
        assertEquals(1, IconPaging.pageCount(0))
        assertEquals(1, IconPaging.pageCount(96))
        assertEquals(2, IconPaging.pageCount(97))
    }

    /**
     * A page beyond the end is clamped to the last page.
     */
    @Test
    fun `page clamps to the last page`() {
        val icons = (0 until 100).map { IconInfo("i$it", emptyList(), emptyList()) }
        assertEquals(4, IconPaging.page(icons, 5).size)
        assertEquals("i96", IconPaging.page(icons, 5).first().name)
        assertEquals(96, IconPaging.page(icons, -3).size)
    }
}

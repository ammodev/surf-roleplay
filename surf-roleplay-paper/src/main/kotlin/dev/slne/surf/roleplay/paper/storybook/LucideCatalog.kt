package dev.slne.surf.roleplay.paper.storybook

import com.google.gson.JsonParser
import dev.slne.surf.api.core.util.logger

/**
 * The logger of a missing icon index.
 */
private val log = logger()

/**
 * One Lucide icon as the gallery lists it.
 *
 * @property name the Lucide name, such as `trash`
 * @property categories the Lucide categories of the icon
 * @property aliases the other names the icon is known by
 */
data class IconInfo(val name: String, val categories: List<String>, val aliases: List<String>)

/**
 * The Lucide icons the mod can draw, with search and category filtering.
 *
 * @property icons the icons sorted by name, without aliases
 */
class LucideCatalog(val icons: List<IconInfo>) {

    /**
     * The categories of all icons, sorted and without duplicates.
     */
    val categories: List<String> = icons.flatMap { it.categories }.distinct().sorted()

    /**
     * Finds the icons whose name or alias contains the query and that belong to the category.
     *
     * @param query the text to look for, ignoring case; empty matches every icon
     * @param category the category the icon must belong to, or `null` for any
     * @return the matching icons in catalog order
     */
    fun search(query: String, category: String?): List<IconInfo> {
        val needle = query.trim().lowercase()
        return icons.filter { icon ->
            (category == null || category in icon.categories) &&
                (needle.isEmpty() || icon.name.contains(needle) || icon.aliases.any { it.contains(needle) })
        }
    }

    /**
     * Parses and loads catalogs.
     */
    companion object {
        /**
         * The location of the bundled index inside the plugin jar.
         */
        private const val RESOURCE = "/lucide/index.json"

        /**
         * Parses the index the mod build generates, folding each alias into its icon.
         *
         * @param json the contents of `index.json`
         * @return the catalog of its icons
         */
        fun parse(json: String): LucideCatalog {
            val entries = JsonParser.parseString(json).asJsonObject.getAsJsonObject("icons")
            val aliases = HashMap<String, MutableList<String>>()
            val icons = ArrayList<IconInfo>()
            for ((name, value) in entries.entrySet()) {
                val entry = value.asJsonObject
                val target = entry.get("alias")?.asString
                if (target != null) {
                    aliases.getOrPut(target) { mutableListOf() }.add(name)
                } else {
                    val categories = entry.getAsJsonArray("categories")?.map { it.asString } ?: emptyList()
                    icons.add(IconInfo(name, categories, emptyList()))
                }
            }
            return LucideCatalog(
                icons.map { it.copy(aliases = aliases[it.name].orEmpty().sorted()) }.sortedBy { it.name }
            )
        }

        /**
         * Loads the index bundled in the plugin jar.
         *
         * @return the catalog, or an empty one, with a logged warning, if the index is missing
         */
        fun load(): LucideCatalog {
            val stream = LucideCatalog::class.java.getResourceAsStream(RESOURCE)
            if (stream == null) {
                log.atWarning().log("The Lucide icon index %s is missing from the plugin jar; the icon gallery is empty", RESOURCE)
                return LucideCatalog(emptyList())
            }
            return stream.use { parse(it.readBytes().toString(Charsets.UTF_8)) }
        }
    }
}

/**
 * Splits icon lists into the fixed-size pages of the gallery.
 */
object IconPaging {
    /**
     * The number of icons on one page.
     */
    const val PAGE_SIZE = 96

    /**
     * Counts the pages needed for a number of icons.
     *
     * @param total the number of icons
     * @return the page count, at least one
     */
    fun pageCount(total: Int): Int = maxOf(1, (total + PAGE_SIZE - 1) / PAGE_SIZE)

    /**
     * Returns one page of icons.
     *
     * @param icons all icons
     * @param page the zero-based page, clamped to the existing pages
     * @return the icons on that page
     */
    fun page(icons: List<IconInfo>, page: Int): List<IconInfo> {
        val index = page.coerceIn(0, pageCount(icons.size) - 1)
        return icons.drop(index * PAGE_SIZE).take(PAGE_SIZE)
    }
}

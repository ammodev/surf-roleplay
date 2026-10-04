package dev.slne.surf.roleplay.fabric.settings

/**
 * Pure rules for the navigation state of the settings screen: which category is selected and how
 * far each category's content is scrolled.
 */
object SettingsNavigation {

    /**
     * Returns the category a tab value selects.
     *
     * @param value the value reported by the tabs, or `null` if there is none
     * @param ids the ids of the categories, in display order
     * @return [value] if it is one of [ids], otherwise the first id
     */
    fun select(value: String?, ids: List<String>): String = value?.takeIf { it in ids } ?: ids.first()
}

/**
 * The vertical scroll offsets of the scrollable content areas of the settings screen, by area id.
 *
 * @property offsets the remembered offsets
 */
data class ScrollOffsets(val offsets: Map<String, Int> = emptyMap()) {

    /**
     * Returns the offset remembered for an area.
     *
     * @param id the id of the area
     * @return its offset, or 0 if none is remembered
     */
    fun of(id: String): Int = offsets[id] ?: 0

    /**
     * Returns a copy that remembers an offset for an area; negative offsets are stored as 0.
     *
     * @param id the id of the area
     * @param offset the offset to remember
     * @return the copy
     */
    fun with(id: String, offset: Int): ScrollOffsets = ScrollOffsets(offsets + (id to offset.coerceAtLeast(0)))
}

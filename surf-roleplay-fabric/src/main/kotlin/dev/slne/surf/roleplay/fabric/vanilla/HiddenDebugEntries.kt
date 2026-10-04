package dev.slne.surf.roleplay.fabric.vanilla

/**
 * The debug screen entries that reveal coordinates, and the filter that removes them while the
 * roleplay server is active.
 */
object HiddenDebugEntries {

    /**
     * The paths of the `minecraft` debug screen entries that print a position of the player, a
     * looked-at block, fluid or entity, or terrain information derived from the position.
     */
    val PATHS: Set<String> = setOf(
        "player_position",
        "player_section_position",
        "looking_at_block_state",
        "looking_at_block_tags",
        "looking_at_fluid_state",
        "looking_at_fluid_tags",
        "looking_at_entity",
        "looking_at_entity_tags",
        "heightmap",
        "light_levels",
        "biome",
    )

    /**
     * Returns [entries] without those for which [isHidden] is true while [active] is true, and
     * [entries] unchanged otherwise.
     *
     * @param entries the entries to filter
     * @param isHidden tells whether an entry is one of the hidden entries
     * @param active whether the roleplay server is active
     * @return the entries that stay visible
     */
    fun <T> visible(entries: Collection<T>, isHidden: (T) -> Boolean, active: Boolean): Collection<T> =
        if (active) entries.filterNot(isHidden) else entries
}

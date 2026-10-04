package dev.slne.surf.roleplay.fabric.tablist

import dev.slne.surf.roleplay.protocol.tablist.OnlineLevel
import dev.slne.surf.roleplay.protocol.tablist.OrganisationCount
import dev.slne.surf.roleplay.protocol.tablist.TabListState
import java.time.ZoneId

/**
 * The content of the tab list panel for one frame, with every text already in German.
 *
 * @property organisations one cell per organisation, in the order the server sent them
 * @property info the cells for the online total, the clock and the weather
 * @property restart the restart countdown, or `null` to hide the row
 * @property announcement the announcement as component JSON, or `null` to hide the row
 * @property footer the cells for the character name, job, rank, playtime and ping
 */
data class TabListView(
    val organisations: List<Cell>,
    val info: List<Cell>,
    val restart: String?,
    val announcement: String?,
    val footer: List<Cell>,
) {

    /**
     * A text of the panel, either plain or component JSON.
     *
     * @property value the text
     * @property json whether [value] is component JSON
     */
    data class Text(val value: String, val json: Boolean = false)

    /**
     * One labelled value of the panel.
     *
     * @property label the label
     * @property value the value
     * @property icon the Lucide name of the icon before the label, empty for none
     */
    data class Cell(val label: Text, val value: Text, val icon: String = "")

    /**
     * Builds the panel content from a state.
     */
    companion object {

        /**
         * The text shown for a missing value.
         */
        const val MISSING: String = "–"

        /**
         * Builds the panel content of a state at a point in time.
         *
         * @param state the last state the server sent
         * @param nowMillis the current server time in epoch milliseconds
         * @param zone the time zone to show the clock in
         * @param ping the round-trip time to the server in milliseconds; `null` or `0` shows a dash for an unknown time
         * @return the panel content
         */
        fun of(state: TabListState, nowMillis: Long, zone: ZoneId, ping: Int?): TabListView = TabListView(
            organisations = state.organisations.map(::organisation),
            info = listOf(
                Cell(Text("Online"), Text(state.onlineTotal.toString())),
                Cell(Text("Uhrzeit"), Text(TabListClock.clock(nowMillis, zone))),
                Cell(Text("Wetter"), if (state.weather.isBlank()) Text(MISSING) else Text(state.weather, json = true)),
            ),
            restart = TabListClock.restart(state.restartAtMillis, nowMillis),
            announcement = state.announcement?.takeIf { it.isNotBlank() },
            footer = listOf(
                Cell(Text("Name"), Text(state.characterName ?: MISSING)),
                Cell(Text("Beruf"), Text(state.job ?: MISSING)),
                Cell(Text("Rang"), Text(state.rank ?: MISSING)),
                Cell(Text("Spielzeit"), Text(TabListClock.playtime(state.sessionStartMillis, nowMillis))),
                Cell(Text("Ping"), Text(ping?.takeIf { it > 0 }?.let { "$it ms" } ?: MISSING)),
            ),
        )

        /**
         * Builds the cell of one organisation: the exact count if the server sent one, the
         * coarse level otherwise.
         *
         * @param count the organisation count
         * @return the cell
         */
        private fun organisation(count: OrganisationCount): Cell =
            Cell(Text(count.label, json = true), Text(count.exact?.toString() ?: level(count.level)), count.icon)

        /**
         * Returns the German word for a coarse online level.
         *
         * @param level the level
         * @return the word
         */
        private fun level(level: OnlineLevel): String = when (level) {
            OnlineLevel.NONE -> "keine"
            OnlineLevel.FEW -> "wenige"
            OnlineLevel.MANY -> "viele"
        }
    }
}

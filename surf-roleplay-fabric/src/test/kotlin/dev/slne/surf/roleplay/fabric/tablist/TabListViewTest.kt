package dev.slne.surf.roleplay.fabric.tablist

import dev.slne.surf.roleplay.protocol.tablist.OnlineLevel
import dev.slne.surf.roleplay.protocol.tablist.OrganisationCount
import dev.slne.surf.roleplay.protocol.tablist.TabListState
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for the layout model of the tab list.
 */
class TabListViewTest {

    /**
     * The time zone of the test states.
     */
    private val berlin: ZoneId = ZoneId.of("Europe/Berlin")

    /**
     * The server time of the test states: 4 October 2026, 20:15 in Berlin.
     */
    private val serverTime: Long = ZonedDateTime.of(2026, 10, 4, 20, 15, 0, 0, berlin).toInstant().toEpochMilli()

    /**
     * A fully filled state.
     */
    private val state = TabListState(
        organisations = listOf(
            OrganisationCount("civilians", """{"text":"Zivilisten"}""", "users", OnlineLevel.MANY),
            OrganisationCount("police", """{"text":"Polizei"}""", "shield", OnlineLevel.FEW),
            OrganisationCount("sar", """{"text":"SAR"}""", "", OnlineLevel.NONE),
        ),
        onlineTotal = 42,
        serverTimeMillis = serverTime,
        zoneId = "Europe/Berlin",
        restartAtMillis = serverTime + (83 * 60_000L),
        weather = """{"text":"Regen"}""",
        announcement = """{"text":"Event um 21 Uhr"}""",
        characterName = "Max Mustermann",
        job = "Taxifahrer",
        rank = "Fahrer",
        sessionStartMillis = serverTime - (65 * 60_000L),
    )

    /**
     * Returns the plain text of a cell value.
     *
     * @param text the text
     * @return the plain text
     */
    private fun plain(text: TabListView.Text): String = text.value

    /**
     * Verifies that the coarse levels are shown in German with the labels and icons of the server.
     */
    @Test
    fun `organisations show the coarse level`() {
        val view = TabListView.of(state, serverTime, berlin, ping = 20)
        assertEquals(
            listOf("viele", "wenige", "keine"),
            view.organisations.map { plain(it.value) },
        )
        assertEquals(TabListView.Text("""{"text":"Zivilisten"}""", json = true), view.organisations[0].label)
        assertEquals("users", view.organisations[0].icon)
        assertEquals("", view.organisations[2].icon)
    }

    /**
     * Verifies that an exact count is shown instead of the level, including zero.
     */
    @Test
    fun `organisations show an exact count when sent`() {
        val exact = state.copy(
            organisations = listOf(
                OrganisationCount("police", """{"text":"Polizei"}""", "", OnlineLevel.FEW, exact = 2),
                OrganisationCount("sar", """{"text":"SAR"}""", "", OnlineLevel.NONE, exact = 0),
            ),
        )
        val view = TabListView.of(exact, serverTime, berlin, ping = 20)
        assertEquals(listOf("2", "0"), view.organisations.map { plain(it.value) })
    }

    /**
     * Verifies that an organisation whose count is unknown shows a dash.
     */
    @Test
    fun `unknown level is a dash`() {
        val unknown = state.copy(organisations = listOf(OrganisationCount("police", """{"text":"Polizei"}""", "shield", OnlineLevel.UNKNOWN)))
        val view = TabListView.of(unknown, serverTime, berlin, ping = 20)
        assertEquals(listOf("–"), view.organisations.map { plain(it.value) })
        assertEquals("shield", view.organisations[0].icon)
    }

    /**
     * Verifies the info cells: online total, clock and weather.
     */
    @Test
    fun `info shows online total, clock and weather`() {
        val view = TabListView.of(state, serverTime + 60_000L, berlin, ping = 20)
        assertEquals(
            listOf(
                TabListView.Cell(TabListView.Text("Online"), TabListView.Text("42")),
                TabListView.Cell(TabListView.Text("Uhrzeit"), TabListView.Text("20:16")),
                TabListView.Cell(TabListView.Text("Wetter"), TabListView.Text("""{"text":"Regen"}""", json = true)),
            ),
            view.info,
        )
    }

    /**
     * Verifies that an empty weather is shown as a dash.
     */
    @Test
    fun `empty weather is a dash`() {
        val view = TabListView.of(state.copy(weather = ""), serverTime, berlin, ping = 20)
        assertEquals(TabListView.Text("–"), view.info[2].value)
    }

    /**
     * Verifies the restart countdown and the announcement when both are set.
     */
    @Test
    fun `restart and announcement are shown when set`() {
        val view = TabListView.of(state, serverTime, berlin, ping = 20)
        assertEquals("Neustart in 1 h 23 min", view.restart)
        assertEquals("""{"text":"Event um 21 Uhr"}""", view.announcement)
    }

    /**
     * Verifies that no restart and no or a blank announcement hide both rows.
     */
    @Test
    fun `restart and announcement are hidden when not set`() {
        assertNull(TabListView.of(state.copy(restartAtMillis = null, announcement = null), serverTime, berlin, 20).restart)
        assertNull(TabListView.of(state.copy(announcement = null), serverTime, berlin, 20).announcement)
        assertNull(TabListView.of(state.copy(announcement = "  "), serverTime, berlin, 20).announcement)
    }

    /**
     * Verifies the footer with every value set.
     */
    @Test
    fun `footer shows name, job, rank, playtime and ping`() {
        val view = TabListView.of(state, serverTime, berlin, ping = 37)
        assertEquals(listOf("Name", "Beruf", "Rang", "Spielzeit", "Ping"), view.footer.map { plain(it.label) })
        assertEquals(listOf("Max Mustermann", "Taxifahrer", "Fahrer", "1:05", "37 ms"), view.footer.map { plain(it.value) })
    }

    /**
     * Verifies that missing footer values are shown as a dash.
     */
    @Test
    fun `missing footer values are a dash`() {
        val view = TabListView.of(state.copy(characterName = null, job = null, rank = null), serverTime, berlin, ping = null)
        assertEquals(listOf("–", "–", "–", "1:05", "–"), view.footer.map { plain(it.value) })
    }

    /**
     * Verifies that a latency of zero, which means no measurement yet, is shown as a dash.
     */
    @Test
    fun `zero ping is a dash`() {
        assertEquals(TabListView.Text("–"), TabListView.of(state, serverTime, berlin, ping = 0).footer[4].value)
    }
}

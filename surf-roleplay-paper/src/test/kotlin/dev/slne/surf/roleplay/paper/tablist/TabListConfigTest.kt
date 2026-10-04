package dev.slne.surf.roleplay.paper.tablist

import org.bukkit.configuration.file.YamlConfiguration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for reading and writing the tab list settings and for the next restart time.
 */
class TabListConfigTest {

    /** The zone restarts are scheduled in. */
    private val berlin = ZoneId.of("Europe/Berlin")

    /**
     * Parses a YAML text into a configuration.
     *
     * @param yaml the YAML text
     * @return the configuration
     */
    private fun yaml(yaml: String) = YamlConfiguration().apply { loadFromString(yaml) }

    /**
     * Returns the epoch milliseconds of a Berlin local date and time.
     */
    private fun berlinMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, berlin).toInstant().toEpochMilli()

    /**
     * Verifies the defaults of a configuration without a tab list section.
     */
    @Test
    fun `missing section uses defaults`() {
        val config = TabListConfig.from(yaml(""))

        assertEquals(LocalTime.of(5, 0), config.restartTime)
        assertNull(config.announcement)
        assertEquals(TabListConfig.Organisation(exact = false, thresholds = listOf(1, 3)), config.organisation("police"))
    }

    /**
     * Verifies that configured values are read.
     */
    @Test
    fun `reads configured values`() {
        val config = TabListConfig.from(
            yaml(
                """
                tab-list:
                  restart-time: "06:30"
                  announcement: "Event um 20 Uhr"
                  organisations:
                    police:
                      exact: true
                      thresholds: [2, 5]
                """.trimIndent()
            )
        )

        assertEquals(LocalTime.of(6, 30), config.restartTime)
        assertEquals("Event um 20 Uhr", config.announcement)
        assertEquals(TabListConfig.Organisation(exact = true, thresholds = listOf(2, 5)), config.organisation("police"))
        assertEquals(TabListConfig.Organisation(exact = false, thresholds = listOf(1, 3)), config.organisation("sar"))
    }

    /**
     * Verifies that an empty restart time and a blank announcement mean none.
     */
    @Test
    fun `empty values mean none`() {
        val config = TabListConfig.from(
            yaml(
                """
                tab-list:
                  restart-time: ""
                  announcement: "  "
                """.trimIndent()
            )
        )

        assertNull(config.restartTime)
        assertNull(config.announcement)
    }

    /**
     * Verifies that invalid thresholds and restart times fall back to their defaults with a
     * warning, keeping the valid values.
     */
    @Test
    fun `invalid values fall back to defaults with a warning`() {
        val warnings = mutableListOf<String>()
        val config = TabListConfig.from(
            yaml(
                """
                tab-list:
                  restart-time: "25:00"
                  announcement: "Hallo"
                  organisations:
                    police:
                      exact: true
                      thresholds: [3, 1]
                    sar:
                      thresholds: [2, 4]
                    zero:
                      thresholds: [0, 4]
                """.trimIndent()
            ),
            warnings::add,
        )

        assertEquals(TabListConfig.DEFAULT_RESTART_TIME, config.restartTime)
        assertEquals("Hallo", config.announcement)
        assertEquals(TabListConfig.Organisation.DEFAULT, config.organisation("police"))
        assertEquals(TabListConfig.Organisation(thresholds = listOf(2, 4)), config.organisation("sar"))
        assertEquals(TabListConfig.Organisation.DEFAULT, config.organisation("zero"))
        assertEquals(3, warnings.size)
    }

    /**
     * Verifies that the restart later today is chosen before it has passed.
     */
    @Test
    fun `next restart today`() {
        val config = TabListConfig(restartTime = LocalTime.of(5, 0))
        assertEquals(berlinMillis(2026, 6, 10, 5, 0), config.nextRestartMillis(berlinMillis(2026, 6, 10, 4, 59)))
    }

    /**
     * Verifies that the restart tomorrow is chosen once today's has passed or is now.
     */
    @Test
    fun `next restart tomorrow`() {
        val config = TabListConfig(restartTime = LocalTime.of(5, 0))
        assertEquals(berlinMillis(2026, 6, 11, 5, 0), config.nextRestartMillis(berlinMillis(2026, 6, 10, 5, 0)))
        assertEquals(berlinMillis(2026, 6, 11, 5, 0), config.nextRestartMillis(berlinMillis(2026, 6, 10, 23, 59)))
    }

    /**
     * Verifies the next restart across midnight and the end of a month.
     */
    @Test
    fun `next restart across midnight`() {
        val config = TabListConfig(restartTime = LocalTime.of(0, 30))
        assertEquals(berlinMillis(2026, 7, 1, 0, 30), config.nextRestartMillis(berlinMillis(2026, 6, 30, 23, 0)))
    }

    /**
     * Verifies the next restart on the day clocks move forward: a time that does not exist moves
     * to the first instant after the gap, and a normal time keeps its wall-clock time.
     */
    @Test
    fun `next restart when clocks move forward`() {
        val beforeGap = berlinMillis(2026, 3, 29, 1, 0)
        assertEquals(berlinMillis(2026, 3, 29, 5, 0), TabListConfig(restartTime = LocalTime.of(5, 0)).nextRestartMillis(beforeGap))
        assertEquals(berlinMillis(2026, 3, 29, 3, 30), TabListConfig(restartTime = LocalTime.of(2, 30)).nextRestartMillis(beforeGap))
    }

    /**
     * Verifies the next restart on the day clocks move back: the wall-clock time is kept.
     */
    @Test
    fun `next restart when clocks move back`() {
        val config = TabListConfig(restartTime = LocalTime.of(5, 0))
        val next = config.nextRestartMillis(berlinMillis(2026, 10, 25, 1, 0))
        assertEquals(berlinMillis(2026, 10, 25, 5, 0), next)
        assertEquals(5 * 60 * 60 * 1000L, next!! - berlinMillis(2026, 10, 25, 1, 0))
    }

    /**
     * Verifies that no restart is reported when none is configured.
     */
    @Test
    fun `no restart`() {
        assertNull(TabListConfig(restartTime = null).nextRestartMillis(0))
    }
}

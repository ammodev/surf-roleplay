package dev.slne.surf.roleplay.paper.tablist

import dev.slne.surf.roleplay.api.client.paper.tablist.OrganisationCountProvider
import dev.slne.surf.roleplay.api.client.paper.tablist.SelfInfoProvider
import dev.slne.surf.roleplay.paper.screen.ScreenMapper
import dev.slne.surf.roleplay.paper.tablist.TabListStateBuilder.SelfValue
import dev.slne.surf.roleplay.protocol.tablist.OnlineLevel
import dev.slne.surf.roleplay.protocol.tablist.OrganisationCount
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.time.LocalTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for building the tab list state from providers, settings and the clock.
 */
class TabListStateBuilderTest {

    /** The current fake time in milliseconds. */
    private var now = 1_000_000L

    /** The providers whose failure was logged, in order. */
    private val logged = mutableListOf<String>()

    /** The builder under test. */
    private val builder = TabListStateBuilder(clock = { now }) { name, _ -> logged += name }

    /**
     * An organisation provider whose members are counted by the test, never by a player.
     *
     * @property members the number of online members, or `null` to throw when counted
     */
    private class FakeOrganisation(override val key: String, val members: Int?) : OrganisationCountProvider {
        /** The key as plain text. */
        override val label: Component = Component.text(key)

        /** A fixed icon. */
        override val icon = "shield"

        /** Never called: the test counts members itself. */
        override fun counts(player: Player): Boolean = error("players are not counted in this test")
    }

    /**
     * Counts the members of a fake organisation, throwing for a broken one.
     */
    private val count: (OrganisationCountProvider) -> Int = { (it as FakeOrganisation).members ?: error("broken") }

    /**
     * A self-info provider whose values are read by the test.
     *
     * @property values the values it knows; a value mapped to an exception is thrown when read
     */
    private class FakeSelfInfo(val values: Map<SelfValue, Any>) : SelfInfoProvider

    /**
     * Reads a value of a fake self-info provider.
     */
    private val read: (SelfInfoProvider, SelfValue) -> String? = { provider, value ->
        when (val known = (provider as FakeSelfInfo).values[value]) {
            is Exception -> throw known
            else -> known as String?
        }
    }

    /**
     * Builds a state with the given self-info providers and otherwise fixed values.
     */
    private fun buildWith(selfInfo: List<SelfInfoProvider>) =
        builder.build(emptyList(), 1, Component.text("Klar"), 0, selfInfo, read, TabListConfig())

    /**
     * Verifies that coarse organisations never carry the exact count.
     */
    @Test
    fun `coarse never sets exact`() {
        val counts = builder.organisations(listOf(FakeOrganisation("police", 2)), count, TabListConfig())

        assertEquals(listOf(OrganisationCount("police", ScreenMapper.text(Component.text("police")), "shield", OnlineLevel.FEW, null)), counts)
    }

    /**
     * Verifies that an organisation configured exact carries its count.
     */
    @Test
    fun `exact when configured`() {
        val config = TabListConfig(organisations = mapOf("police" to TabListConfig.Organisation(exact = true)))
        val counts = builder.organisations(listOf(FakeOrganisation("police", 3), FakeOrganisation("sar", 0)), count, config)

        assertEquals(OnlineLevel.MANY, counts[0].level)
        assertEquals(3, counts[0].exact)
        assertEquals(OnlineLevel.NONE, counts[1].level)
        assertNull(counts[1].exact)
    }

    /**
     * Verifies that configured thresholds are applied.
     */
    @Test
    fun `configured thresholds`() {
        val config = TabListConfig(organisations = mapOf("police" to TabListConfig.Organisation(thresholds = listOf(3, 5))))
        val counts = builder.organisations(listOf(FakeOrganisation("police", 2)), count, config)

        assertEquals(OnlineLevel.NONE, counts.single().level)
    }

    /**
     * Verifies that an organisation whose provider throws keeps its row with the unknown level and
     * no exact count, the others are kept, and the failure is logged at most once per minute.
     */
    @Test
    fun `throwing organisation is unknown and logged once per minute`() {
        val providers = listOf(FakeOrganisation("broken", null), FakeOrganisation("sar", 1))
        val exact = TabListConfig(organisations = mapOf("broken" to TabListConfig.Organisation(exact = true)))

        repeat(3) {
            val counts = builder.organisations(providers, count, exact)
            assertEquals(OrganisationCount("broken", ScreenMapper.text(Component.text("broken")), "shield", OnlineLevel.UNKNOWN, null), counts[0])
            assertEquals(OnlineLevel.FEW, counts[1].level)
        }
        assertEquals(listOf("organisation 'broken'"), logged)

        now += 59_999
        builder.organisations(providers, count, TabListConfig())
        assertEquals(1, logged.size)

        now += 1
        builder.organisations(providers, count, TabListConfig())
        assertEquals(2, logged.size)
    }

    /**
     * Verifies that a provider whose label and icon throw still gets a row, labelled with its key
     * and without an icon, and that a provider whose key and count throw is keyed by its class name
     * with the unknown level.
     */
    @Test
    fun `throwing label and key fall back`() {
        val broken = object : OrganisationCountProvider {
            override val key: String get() = error("broken")
            override val label: Component get() = error("broken")
            override val icon: String get() = error("broken")
            override fun counts(player: Player): Boolean = error("unused")
        }
        val noLabel = object : OrganisationCountProvider {
            override val key = "police"
            override val label: Component get() = error("broken")
            override val icon: String get() = error("broken")
            override fun counts(player: Player): Boolean = error("unused")
        }

        val counts = builder.organisations(listOf(noLabel, broken), { if (it === broken) error("broken") else 1 }, TabListConfig())

        assertEquals(OrganisationCount("police", ScreenMapper.text(Component.text("police")), "", OnlineLevel.FEW), counts[0])
        assertEquals(broken.javaClass.name, counts[1].key)
        assertEquals(OnlineLevel.UNKNOWN, counts[1].level)
    }

    /**
     * Verifies the state without any providers, announcement or restart.
     */
    @Test
    fun `missing providers give empty values`() {
        val state = builder.build(
            organisations = emptyList(),
            onlineTotal = 3,
            weather = Component.text("Klar"),
            sessionStartMillis = 500,
            selfInfo = emptyList(),
            read = read,
            config = TabListConfig(restartTime = null),
        )

        assertTrue(state.organisations.isEmpty())
        assertEquals(3, state.onlineTotal)
        assertEquals(now, state.serverTimeMillis)
        assertEquals("Europe/Berlin", state.zoneId)
        assertNull(state.restartAtMillis)
        assertEquals(ScreenMapper.text(Component.text("Klar")), state.weather)
        assertNull(state.announcement)
        assertNull(state.characterName)
        assertNull(state.job)
        assertNull(state.rank)
        assertEquals(500, state.sessionStartMillis)
    }

    /**
     * Verifies that the announcement, the restart and the given organisations are carried over.
     */
    @Test
    fun `announcement restart and organisations`() {
        val organisations = listOf(OrganisationCount("police", "{}", level = OnlineLevel.FEW))
        val config = TabListConfig(restartTime = LocalTime.of(5, 0), announcement = "Event")

        val state = builder.build(organisations, 3, Component.text("Regen"), 0, emptyList(), read, config)

        assertEquals(organisations, state.organisations)
        assertEquals(config.nextRestartMillis(now), state.restartAtMillis)
        assertEquals(ScreenMapper.text(Component.text("Event")), state.announcement)
    }

    /**
     * Verifies that the online total is rounded to the nearest five and flagged approximate while
     * any organisation is coarse.
     */
    @Test
    fun `online total is approximate while any organisation is coarse`() {
        val organisations = listOf(OrganisationCount("police", "{}"), OrganisationCount("sar", "{}"))
        val config = TabListConfig(organisations = mapOf("police" to TabListConfig.Organisation(exact = true)))

        val state = builder.build(organisations, 42, Component.text("Klar"), 0, emptyList(), read, config)

        assertEquals(40, state.onlineTotal)
        assertTrue(state.onlineTotalApproximate)
        assertEquals(listOf(0, 0, 0, 5, 5, 5, 5, 5, 10), (0..8).map(TabListStateBuilder::approximateTotal))
    }

    /**
     * Verifies that the online total is exact when every organisation is exact.
     */
    @Test
    fun `online total is exact when every organisation is exact`() {
        val organisations = listOf(OrganisationCount("police", "{}"), OrganisationCount("sar", "{}"))
        val config = TabListConfig(
            organisations = mapOf(
                "police" to TabListConfig.Organisation(exact = true),
                "sar" to TabListConfig.Organisation(exact = true),
            ),
        )

        val state = builder.build(organisations, 42, Component.text("Klar"), 0, emptyList(), read, config)

        assertEquals(42, state.onlineTotal)
        assertFalse(state.onlineTotalApproximate)
    }

    /**
     * Verifies that each self value comes from the first provider that knows it.
     */
    @Test
    fun `self info from the first provider that knows it`() {
        val identity = FakeSelfInfo(mapOf(SelfValue.RANK to "Polizeimeister"))
        val jobs = FakeSelfInfo(mapOf(SelfValue.JOB to "Taxifahrer", SelfValue.RANK to "Fahrer"))

        val state = buildWith(listOf(identity, jobs))

        assertNull(state.characterName)
        assertEquals("Taxifahrer", state.job)
        assertEquals("Polizeimeister", state.rank)
    }

    /**
     * Verifies that a throwing self-info provider yields no value, lets later providers answer,
     * and is logged at most once per minute.
     */
    @Test
    fun `throwing self info provider is skipped and logged once per minute`() {
        val broken = FakeSelfInfo(mapOf(SelfValue.JOB to IllegalStateException("broken"), SelfValue.RANK to IllegalStateException("broken")))
        val working = FakeSelfInfo(mapOf(SelfValue.RANK to "Notarzt"))

        val state = buildWith(listOf(broken, working))
        buildWith(listOf(broken, working))

        assertNull(state.job)
        assertEquals("Notarzt", state.rank)
        assertEquals(1, logged.size)
    }

    /**
     * Verifies that an error other than a virtual machine error is treated as a provider failure,
     * and that a virtual machine error is passed on.
     */
    @Test
    fun `errors are failures but virtual machine errors pass`() {
        val counts = builder.organisations(listOf(FakeOrganisation("police", 1)), { throw NotImplementedError() }, TabListConfig())
        assertEquals(OnlineLevel.UNKNOWN, counts.single().level)

        assertFailsWith<StackOverflowError> {
            builder.organisations(listOf(FakeOrganisation("police", 1)), { throw StackOverflowError() }, TabListConfig())
        }
    }

    /**
     * Verifies that players who are leaving are excluded from the counted players.
     */
    @Test
    fun `leaving players are not counted`() {
        val staying = UUID.randomUUID()
        val leaving = UUID.randomUUID()

        assertEquals(listOf(staying), TabListStateBuilder.present(listOf(staying, leaving), { it }, setOf(leaving)))
        assertEquals(listOf(staying, leaving), TabListStateBuilder.present(listOf(staying, leaving), { it }, emptySet()))
    }

    /**
     * Verifies the German weather texts.
     */
    @Test
    fun `weather texts`() {
        assertEquals(Component.text("Klar"), TabListStateBuilder.weather(storm = false, thundering = false))
        assertEquals(Component.text("Regen"), TabListStateBuilder.weather(storm = true, thundering = false))
        assertEquals(Component.text("Gewitter"), TabListStateBuilder.weather(storm = true, thundering = true))
    }
}

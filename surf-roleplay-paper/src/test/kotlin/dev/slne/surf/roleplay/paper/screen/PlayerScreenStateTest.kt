package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.CounterAction
import dev.slne.surf.roleplay.protocol.screen.CounterState
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.ScreenClose
import dev.slne.surf.roleplay.protocol.screen.ScreenClosed
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenPatch
import dev.slne.surf.roleplay.protocol.screen.ScreenTypedAction
import dev.slne.surf.roleplay.protocol.screen.ScreenTypedUpdate
import dev.slne.surf.roleplay.protocol.screen.ScreenTypes
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.SetText
import dev.slne.surf.roleplay.protocol.screen.TypedScreenBody
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [PlayerScreenState].
 */
class PlayerScreenStateTest {

    /**
     * The packets sent to the client, in order.
     */
    private val sent = mutableListOf<Packet>()

    /**
     * A sender that records every packet.
     */
    private val sender = object : ScreenPacketSender {
        /**
         * Records a packet.
         *
         * @param type the packet type
         * @param packet the packet
         */
        override fun <P : Packet> send(type: PacketType<P>, packet: P) {
            sent += packet
        }
    }

    /**
     * The current time of the fake clock.
     */
    private var now = 0L

    /**
     * Whether the fake thread check passes.
     */
    private var onOwningThread = true

    /**
     * The state under test, with a limit of five actions per second and a fake thread check.
     */
    private val state = PlayerScreenState(UUID.randomUUID(), sender, ActionRateLimiter(5) { now }) {
        check(onOwningThread) { "wrong thread" }
    }

    /**
     * The names of the screens whose close handler ran, in order.
     */
    private val closed = mutableListOf<String>()

    /**
     * The clicks received by button handlers.
     */
    private val clicks = mutableListOf<ScreenClick>()

    /**
     * Creates a screen with a name input and a submit button whose handler records clicks.
     *
     * @param name the name recorded when the screen closes
     * @return the definition
     */
    private fun form(name: String) = screen(Component.text(name)) {
        onClose { closed += name }
        column("root") {
            label("title", Component.text(name))
            textInput("name", maxLength = 8)
            button("submit", Component.text("OK")) { clicks += it }
        }
    }

    /**
     * Verifies that opening sends the mapped screen with increasing session ids and the parent.
     */
    @Test
    fun `open sends the screen with its session and parent`() {
        val first = state.open(form("a"), null)
        val second = state.open(form("b"), first.sessionId)

        val packet = assertIs<ScreenOpen>(sent[1])
        assertEquals(second.sessionId, packet.sessionId)
        assertEquals(first.sessionId, packet.parentSessionId)
        assertTrue(second.sessionId > first.sessionId)
        assertEquals("root", assertIs<WidgetScreenBody>(packet.body).root.id)
    }

    /**
     * Verifies that opening without a parent closes the open screens and runs their close
     * handlers, without an extra close packet.
     */
    @Test
    fun `open without parent replaces the open screens`() {
        val first = state.open(form("a"), null)
        state.open(form("b"), first.sessionId)

        state.open(form("c"), null)

        assertEquals(listOf("b", "a"), closed)
        assertFalse(first.isOpen)
        assertEquals(1, state.openScreens.size)
        assertTrue(sent.none { it is ScreenClose })
    }

    /**
     * Verifies that closing a screen closes the screens above it, runs their close handlers and
     * tells the client.
     */
    @Test
    fun `close closes the screens above and tells the client`() {
        val first = state.open(form("a"), null)
        val second = state.open(form("b"), first.sessionId)

        first.close()

        assertEquals(listOf("b", "a"), closed)
        assertFalse(second.isOpen)
        assertEquals(ScreenClose(first.sessionId), sent.last())
    }

    /**
     * Verifies that a close by the player runs the close handlers without echoing a close.
     */
    @Test
    fun `close by the player runs handlers without echo`() {
        val first = state.open(form("a"), null)

        state.handleClosed(ScreenClosed(first.sessionId))

        assertEquals(listOf("a"), closed)
        assertTrue(sent.none { it is ScreenClose })
    }

    /**
     * Verifies that a patch updates the server tree and sends only the applied operations, and
     * that patching a closed screen sends nothing.
     */
    @Test
    fun `patch sends the applied operations`() {
        val screen = state.open(form("a"), null)

        screen.patch {
            setText("title", Component.text("Neu"))
            setText("missing", Component.text("x"))
        }
        val patch = assertIs<ScreenPatch>(sent.last())
        assertEquals(listOf(SetText("title", ScreenMapper.text(Component.text("Neu")))), patch.operations)

        screen.close()
        val count = sent.size
        screen.patch { setText("title", Component.text("Zu")) }
        assertEquals(count, sent.size)
    }

    /**
     * Verifies that a valid click runs the button handler with the validated values and stores
     * them in the server tree.
     */
    @Test
    fun `valid click runs the handler`() {
        val screen = state.open(form("a"), null)

        val outcome = state.handleWidgetAction(ScreenWidgetAction(screen.sessionId, "submit", listOf(InputValue("name", "Max"))))

        assertIs<PlayerScreenState.Outcome.Accepted>(outcome)
        assertEquals("Max", clicks.single().values.text("name"))
        assertEquals(screen, clicks.single().screen)
    }

    /**
     * Verifies that clicks for unknown sessions and invalid values are rejected without running
     * the handler.
     */
    @Test
    fun `invalid clicks are rejected`() {
        val screen = state.open(form("a"), null)

        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleWidgetAction(ScreenWidgetAction(99, "submit")))
        assertIs<PlayerScreenState.Outcome.Rejected>(
            state.handleWidgetAction(ScreenWidgetAction(screen.sessionId, "submit", listOf(InputValue("name", "zu lang für acht")))),
        )
        assertTrue(clicks.isEmpty())
    }

    /**
     * Verifies that actions beyond the rate limit are rejected.
     */
    @Test
    fun `actions beyond the rate limit are rejected`() {
        val screen = state.open(form("a"), null)
        val action = ScreenWidgetAction(screen.sessionId, "submit")

        val outcomes = List(6) { state.handleWidgetAction(action) }

        assertEquals(5, outcomes.count { it is PlayerScreenState.Outcome.Accepted })
        assertIs<PlayerScreenState.Outcome.Rejected>(outcomes.last())
    }

    /**
     * Verifies that a typed screen opens with its encoded state, routes decoded actions to its
     * handler, and sends typed updates.
     */
    @Test
    fun `typed screens route actions and send updates`() {
        val actions = mutableListOf<CounterAction>()
        val typed = state.openTyped(
            ScreenTypes.DEBUG_COUNTER,
            Component.text("Zähler"),
            CounterState(1),
            closable = true,
            parentSessionId = null,
            onAction = { _, action -> actions += action },
        )
        val body = assertIs<TypedScreenBody>(assertIs<ScreenOpen>(sent.last()).body)
        assertEquals(CounterState(1), ScreenTypes.DEBUG_COUNTER.decodeState(body.state))

        state.handleTypedAction(ScreenTypedAction(typed.sessionId, ScreenTypes.DEBUG_COUNTER.encodeAction(CounterAction(2))))
        typed.update(CounterState(3))

        assertEquals(listOf(CounterAction(2)), actions)
        val update = assertIs<ScreenTypedUpdate>(sent.last())
        assertEquals(CounterState(3), ScreenTypes.DEBUG_COUNTER.decodeState(update.state))
    }

    /**
     * Verifies that a typed action with undecodable bytes or for a generic screen is rejected.
     */
    @Test
    fun `invalid typed actions are rejected`() {
        val generic = state.open(form("a"), null)

        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleTypedAction(ScreenTypedAction(generic.sessionId, byteArrayOf(1))))
    }

    /**
     * Verifies that closing everything without telling the client runs every close handler.
     */
    @Test
    fun `close all runs every close handler`() {
        val first = state.open(form("a"), null)
        state.open(form("b"), first.sessionId)
        val count = sent.size

        state.closeAll(notifyClient = false)

        assertEquals(listOf("b", "a"), closed)
        assertTrue(state.openScreens.isEmpty())
        assertEquals(count, sent.size)
    }

    /**
     * Verifies that a label node is sent for a label element, as a check on the mapping in open.
     */
    @Test
    fun `open maps elements to nodes`() {
        state.open(screen(Component.text("x")) { label("only", Component.text("L")) }, null)

        val root = assertIs<WidgetScreenBody>(assertIs<ScreenOpen>(sent.last()).body).root
        assertEquals(LabelNode("only", text = ScreenMapper.text(Component.text("L"))), root)
    }

    /**
     * Verifies that actions and close reports for a screen covered by a child are rejected.
     */
    @Test
    fun `covered screens do not accept actions`() {
        val parent = state.open(form("a"), null)
        state.open(form("b"), parent.sessionId)

        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleWidgetAction(ScreenWidgetAction(parent.sessionId, "submit")))
        state.handleClosed(ScreenClosed(parent.sessionId))

        assertTrue(clicks.isEmpty())
        assertTrue(parent.isOpen)
    }

    /**
     * Verifies that a close report for a screen that is not closable is ignored.
     */
    @Test
    fun `close reports for non-closable screens are ignored`() {
        val locked = state.open(screen(Component.text("fest")) { closable = false; label("l", Component.empty()) }, null)

        state.handleClosed(ScreenClosed(locked.sessionId))

        assertTrue(locked.isOpen)
    }

    /**
     * Verifies that a close handler that opens a screen while another screen replaces its own
     * leaves the client and server with the same top screen.
     */
    @Test
    fun `close handlers that open screens keep both stacks in step`() {
        val hub = screen(Component.text("hub")) { label("hub", Component.empty()) }
        state.open(screen(Component.text("a")) { onClose { state.open(hub, null) }; label("a", Component.empty()) }, null)

        state.open(form("b"), null)

        val lastOpen = sent.filterIsInstance<ScreenOpen>().last()
        assertEquals(state.openScreens.single().sessionId, lastOpen.sessionId)
        assertEquals("hub", assertIs<WidgetScreenBody>(lastOpen.body).root.id)
    }

    /**
     * Verifies that disposing runs every close handler and that screens opened afterwards, even by
     * a close handler, are refused.
     */
    @Test
    fun `dispose runs close handlers and refuses new screens`() {
        var reopenFailed = false
        state.open(screen(Component.text("a")) {
            onClose { reopenFailed = runCatching { state.open(form("late"), null) }.isFailure }
            label("a", Component.empty())
        }, null)

        state.dispose()

        assertTrue(reopenFailed)
        assertTrue(state.openScreens.isEmpty())
        kotlin.test.assertFailsWith<IllegalStateException> { state.open(form("b"), null) }
    }

    /**
     * Verifies that every entry point checks that it runs on the player's thread.
     */
    @Test
    fun `entry points check the owning thread`() {
        val screen = state.open(form("a"), null)
        onOwningThread = false

        kotlin.test.assertFailsWith<IllegalStateException> { state.open(form("b"), null) }
        kotlin.test.assertFailsWith<IllegalStateException> { screen.patch { setText("title", Component.empty()) } }
        kotlin.test.assertFailsWith<IllegalStateException> { screen.close() }
        kotlin.test.assertFailsWith<IllegalStateException> { screen.isOpen }
    }
}

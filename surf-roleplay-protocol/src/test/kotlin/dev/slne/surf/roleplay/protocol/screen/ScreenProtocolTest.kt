package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.ConnectionPhase
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketDirection
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for the screen model and the screen packets.
 */
class ScreenProtocolTest {

    /**
     * A tree that holds every container and widget kind, with non-default values.
     */
    private val tree: ScreenNode = ColumnNode(
        id = "root",
        width = Sizing.grow(1),
        height = Sizing.fixed(200),
        gap = 4,
        padding = Insets(1, 2, 3, 4),
        mainAlign = Align.CENTER,
        crossAlign = Align.STRETCH,
        children = listOf(
            LabelNode("title", text = """{"text":"Hallo","bold":true}"""),
            RowNode(
                id = "inputs",
                children = listOf(
                    TextInputNode("name", value = "Max", placeholder = """{"text":"Name"}""", maxLength = 16, required = true),
                    NumberInputNode("age", value = 30, min = 18, max = 99, required = true, enabled = false),
                    CheckboxNode("agree", label = """{"text":"Ja"}""", checked = true),
                    SelectNode(
                        "city",
                        groups = listOf(SelectGroup(options = listOf(SelectOption("north", """{"text":"Nord"}"""), SelectOption("south", """{"text":"Süd"}""")))),
                        selected = "south",
                        required = true,
                    ),
                ),
            ),
            ScrollListNode(
                id = "list",
                height = Sizing.fixed(80),
                gap = 2,
                children = listOf(LabelNode("row1", text = """{"text":"1"}"""), ButtonNode("row1_open", text = """{"text":"Öffnen"}""")),
            ),
            ImageNode("logo", width = Sizing.fixed(32), height = Sizing.fixed(32), texture = "surf-roleplay:textures/gui/logo.png"),
            ProgressNode("load", progress = 0.25f, label = """{"text":"25%"}"""),
            ButtonNode("submit", text = """{"text":"Senden"}""", enabled = false),
            ButtonNode("back", text = """{"text":"Zurück"}""", submitsInput = false),
        ),
    )

    /**
     * Encodes and decodes a packet through its packet type's channel.
     *
     * @param type the packet type
     * @param packet the packet to round-trip
     * @return the decoded packet
     */
    private fun <P : Packet> roundTrip(type: PacketType<P>, packet: P): Packet =
        ProtocolCodec.decode(type.channel, ProtocolCodec.encode(type, packet))

    /**
     * Verifies that opening a widget screen with a tree of every node kind round-trips.
     */
    @Test
    fun `screen open with a widget tree round-trips`() {
        val packet = ScreenOpen(
            sessionId = 7,
            parentSessionId = 3,
            title = """{"text":"Demo"}""",
            closable = false,
            body = WidgetScreenBody(tree),
        )

        assertEquals(packet, roundTrip(Packets.SCREEN_OPEN, packet))
    }

    /**
     * Verifies that opening a screen without a parent keeps the parent empty and the screen
     * closable.
     */
    @Test
    fun `screen open defaults to no parent and closable`() {
        val packet = ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(LabelNode("a")))

        val decoded = assertIs<ScreenOpen>(roundTrip(Packets.SCREEN_OPEN, packet))

        assertEquals(null, decoded.parentSessionId)
        assertEquals(true, decoded.closable)
    }

    /**
     * Verifies that opening a typed screen keeps its type key and state bytes.
     */
    @Test
    fun `screen open with a typed body round-trips`() {
        val state = ScreenTypes.DEBUG_COUNTER.encodeState(CounterState(5))
        val packet = ScreenOpen(sessionId = 2, title = "{}", body = TypedScreenBody(ScreenTypes.DEBUG_COUNTER.key, state))

        val body = assertIs<TypedScreenBody>(assertIs<ScreenOpen>(roundTrip(Packets.SCREEN_OPEN, packet)).body)

        assertEquals("debug_counter", body.typeKey)
        assertEquals(CounterState(5), ScreenTypes.DEBUG_COUNTER.decodeState(body.state))
    }

    /**
     * Verifies that a patch with every operation kind round-trips in order.
     */
    @Test
    fun `screen patch with every operation round-trips`() {
        val packet = ScreenPatch(
            sessionId = 4,
            operations = listOf(
                ReplaceNode("title", LabelNode("title", text = """{"text":"Neu"}""")),
                InsertNode("list", 1, ButtonNode("row2_open")),
                RemoveNode("logo"),
                SetText("submit", """{"text":"OK"}"""),
                SetValue("name", "Erika"),
                SetProgress("load", 0.75f),
                SetEnabled("submit", true),
            ),
        )

        assertEquals(packet, roundTrip(Packets.SCREEN_PATCH, packet))
    }

    /**
     * Verifies that a typed update keeps its state bytes.
     */
    @Test
    fun `screen typed update round-trips`() {
        val packet = ScreenTypedUpdate(9, byteArrayOf(1, 2, 3))

        val decoded = assertIs<ScreenTypedUpdate>(roundTrip(Packets.SCREEN_TYPED_UPDATE, packet))

        assertEquals(9, decoded.sessionId)
        assertContentEquals(byteArrayOf(1, 2, 3), decoded.state)
    }

    /**
     * Verifies that closing one session and closing every session both round-trip.
     */
    @Test
    fun `screen close round-trips with and without a session`() {
        assertEquals(ScreenClose(5), roundTrip(Packets.SCREEN_CLOSE, ScreenClose(5)))
        assertEquals(ScreenClose(null), roundTrip(Packets.SCREEN_CLOSE, ScreenClose(null)))
    }

    /**
     * Verifies that a widget action with input values round-trips.
     */
    @Test
    fun `screen widget action round-trips`() {
        val packet = ScreenWidgetAction(
            sessionId = 3,
            widgetId = "submit",
            values = listOf(InputValue("name", "Max"), InputValue("age", "30"), InputValue("agree", "true")),
        )

        assertEquals(packet, roundTrip(Packets.SCREEN_WIDGET_ACTION, packet))
    }

    /**
     * Verifies that a typed action keeps its action bytes and decodes as a counter action.
     */
    @Test
    fun `screen typed action round-trips`() {
        val packet = ScreenTypedAction(3, ScreenTypes.DEBUG_COUNTER.encodeAction(CounterAction(-1)))

        val decoded = assertIs<ScreenTypedAction>(roundTrip(Packets.SCREEN_TYPED_ACTION, packet))

        assertEquals(CounterAction(-1), ScreenTypes.DEBUG_COUNTER.decodeAction(decoded.action))
    }

    /**
     * Verifies that a closed notice round-trips.
     */
    @Test
    fun `screen closed round-trips`() {
        assertEquals(ScreenClosed(8), roundTrip(Packets.SCREEN_CLOSED, ScreenClosed(8)))
    }

    /**
     * Verifies that every screen packet is a play-phase packet travelling in its direction.
     */
    @Test
    fun `screen packets use the play phase and their direction`() {
        val clientbound = listOf(Packets.SCREEN_OPEN, Packets.SCREEN_PATCH, Packets.SCREEN_TYPED_UPDATE, Packets.SCREEN_CLOSE)
        val serverbound = listOf(Packets.SCREEN_WIDGET_ACTION, Packets.SCREEN_TYPED_ACTION, Packets.SCREEN_CLOSED)

        for (type in clientbound + serverbound) {
            assertEquals(setOf(ConnectionPhase.PLAY), type.phases, type.channel)
        }
        clientbound.forEach { assertEquals(PacketDirection.CLIENTBOUND, it.direction, it.channel) }
        serverbound.forEach { assertEquals(PacketDirection.SERVERBOUND, it.direction, it.channel) }
    }

    /**
     * Verifies that no two screen types share a key and that each can be found by its key.
     */
    @Test
    fun `screen type keys are unique`() {
        val keys = ScreenTypes.all.map { it.key }

        assertEquals(keys.size, keys.toSet().size)
        ScreenTypes.all.forEach { assertEquals(it, ScreenTypes.byKey(it.key)) }
    }
}

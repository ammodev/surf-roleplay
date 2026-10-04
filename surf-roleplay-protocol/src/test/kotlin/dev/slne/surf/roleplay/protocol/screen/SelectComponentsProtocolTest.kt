package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for selects, comboboxes, option patches and search events on the wire.
 */
class SelectComponentsProtocolTest {

    /**
     * Two option groups with a heading and a disabled option.
     */
    private val groups = listOf(
        SelectGroup("\"Obst\"", listOf(SelectOption("apple", "\"Apfel\""), SelectOption("pear", "\"Birne\"", enabled = false))),
        SelectGroup(options = listOf(SelectOption("carrot", "\"Möhre\""))),
    )

    /**
     * Encodes and decodes a tree inside a screen open.
     *
     * @param root the tree
     * @return the decoded tree
     */
    private fun roundTrip(root: ScreenNode): ScreenNode {
        val open = ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(root))
        val decoded = ProtocolCodec.decode(Packets.SCREEN_OPEN.channel, ProtocolCodec.encode(Packets.SCREEN_OPEN, open)) as ScreenOpen
        return (decoded.body as WidgetScreenBody).root
    }

    /**
     * Verifies that every selection component survives a round trip with all its settings.
     */
    @Test
    fun `selection components round-trip`() {
        val root = ColumnNode(
            "root",
            children = listOf(
                SelectNode("s", groups = groups, selected = "apple", placeholder = "\"Wähle\"", size = SelectSize.SM, required = true, notifyChange = true),
                ComboboxNode(
                    "c",
                    groups = groups,
                    selected = listOf("carrot", "apple"),
                    multiple = true,
                    placeholder = "\"Suche\"",
                    emptyText = "\"Nichts gefunden\"",
                    showClear = true,
                    required = true,
                    notifyChange = true,
                    notifySearch = true,
                ),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that an option patch and a search event survive a round trip, and that value
     * changes carry no query.
     */
    @Test
    fun `option patches and search events round-trip`() {
        val patch = ScreenPatch(sessionId = 2, operations = listOf(SetOptions("c", groups)))
        assertEquals(patch, ProtocolCodec.decode(Packets.SCREEN_PATCH.channel, ProtocolCodec.encode(Packets.SCREEN_PATCH, patch)))

        val search = ScreenInputChange(2, "c", "apple", query = "ap")
        assertEquals(search, ProtocolCodec.decode(Packets.SCREEN_INPUT_CHANGE.channel, ProtocolCodec.encode(Packets.SCREEN_INPUT_CHANGE, search)))
        assertNull(ScreenInputChange(2, "c", "apple").query)
    }
}

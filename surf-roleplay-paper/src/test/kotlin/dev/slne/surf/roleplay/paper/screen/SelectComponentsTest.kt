package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ComboboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenSearch
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.SelectElement
import dev.slne.surf.roleplay.api.client.common.screen.SelectSize
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MultiCombobox
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.ComboboxNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenPatch
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.SelectNode
import dev.slne.surf.roleplay.protocol.screen.SetOptions
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import dev.slne.surf.roleplay.protocol.screen.SelectSize as NodeSelectSize

/**
 * Tests for selects and comboboxes in the API and on Paper.
 */
class SelectComponentsTest {

    /**
     * The packets sent to the client.
     */
    private val sent = mutableListOf<Packet>()

    /**
     * The state under test.
     */
    private val state = PlayerScreenState(
        UUID.randomUUID(),
        object : ScreenPacketSender {
            /**
             * Records a packet.
             *
             * @param type the packet type
             * @param packet the packet
             */
            override fun <P : Packet> send(type: PacketType<P>, packet: P) {
                sent += packet
            }
        },
        ActionRateLimiter(100),
    )

    /**
     * The option groups used by the tests.
     */
    private val groups = listOf(
        SelectChoiceGroup(Component.text("Obst"), listOf(SelectChoice("apple", Component.text("Apfel")), SelectChoice("pear", Component.text("Birne"), enabled = false))),
        SelectChoiceGroup(null, listOf(SelectChoice("carrot", Component.text("Möhre")))),
    )

    /**
     * The searches the combobox reported.
     */
    private val searches = mutableListOf<ScreenSearch>()

    /**
     * Opens a screen with every selection component.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        Screen(Component.text("Auswahl")) {
            Column(id = "root") {
                Select(groups = groups, placeholder = Component.text("Wähle"), size = SelectSize.SM, id = "fruit")
                MultiCombobox(groups = groups, selected = listOf("carrot"), showClear = true, onSearch = { search ->
                    searches += search
                    search.screen.patch { setOptions("tags", listOf(SelectChoiceGroup(null, listOf(SelectChoice("kiwi", Component.text("Kiwi")))))) }
                }, id = "tags")
                Button(Component.text("Senden"), id = "submit")
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that the elements map to their nodes with all their settings.
     */
    @Test
    fun `elements map to their nodes`() {
        open()

        val children = assertIs<ColumnNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root).children
        val select = assertIs<SelectNode>(children[0])
        assertEquals(NodeSelectSize.SM, select.size)
        assertEquals(2, select.groups.size)
        assertNotNull(select.groups[0].label)
        assertNull(select.groups[1].label)
        assertEquals(false, select.groups[0].options[1].enabled)
        val combobox = assertIs<ComboboxNode>(children[1])
        assertEquals(listOf("carrot"), combobox.selected)
        assertEquals(true, combobox.multiple)
        assertEquals(true, combobox.notifySearch)
        assertEquals(true, combobox.showClear)
    }

    /**
     * Verifies that a search event runs the search handler, whose option patch keeps the selected
     * options in an extra group.
     */
    @Test
    fun `searches run the handler and keep the selection`() {
        val session = open()

        val outcome = state.handleInputChange(ScreenInputChange(session, "tags", "carrot", query = "ki"))

        assertIs<PlayerScreenState.Outcome.Accepted>(outcome)
        assertEquals("ki", searches.single().query)
        assertEquals(listOf("kiwi"), assertIs<SetOptions>(assertIs<ScreenPatch>(sent.last()).operations.single()).groups.single().options.map { it.value })
        assertIs<PlayerScreenState.Outcome.Accepted>(
            state.handleWidgetAction(ScreenWidgetAction(session, "submit", listOf(InputValue("tags", "kiwi,carrot")))),
        )
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "fruit", "", query = "a")))
    }

    /**
     * Verifies the select rules: known options only, no newly selected disabled option, and
     * required.
     */
    @Test
    fun `selects validate their options`() {
        val select = SelectElement("s", groups)
        val rule = ElementRules.input(select)!!

        assertNull(rule.violation(select, "apple"))
        assertNull(rule.violation(select, ""))
        assertNotNull(rule.violation(select, "x"))
        assertNotNull(rule.violation(select, "pear"))
        assertNull(rule.violation(select.copy(selected = "pear"), "pear"))
        assertNotNull(rule.violation(select.copy(required = true), ""))
    }

    /**
     * Verifies the combobox rules: a single combobox takes one option, a multiple one several
     * without repeats, and the value keeps the selection order.
     */
    @Test
    fun `comboboxes validate their selection`() {
        val single = ComboboxElement("c", groups)
        val multiple = single.copy(multiple = true)
        val rule = ElementRules.input(single)!!

        assertNull(rule.violation(single, "apple"))
        assertNotNull(rule.violation(single, "apple,carrot"))
        assertNull(rule.violation(multiple, "carrot,apple"))
        assertNotNull(rule.violation(multiple, "apple,apple"))
        assertNotNull(rule.violation(multiple, "apple,x"))
        assertEquals("carrot,apple", rule.current(rule.withValue(multiple, "carrot,apple")!!))
    }

    /**
     * Verifies the constraints the API checks when elements are created.
     */
    @Test
    fun `elements check their settings`() {
        assertFailsWith<IllegalArgumentException> { SelectElement("s", groups, selected = "x") }
        assertFailsWith<IllegalArgumentException> { SelectElement("s", groups + groups) }
        assertFailsWith<IllegalArgumentException> { ComboboxElement("c", groups, selected = listOf("apple", "carrot")) }
        assertFailsWith<IllegalArgumentException> { ComboboxElement("c", groups, selected = listOf("apple", "apple"), multiple = true) }
    }

}

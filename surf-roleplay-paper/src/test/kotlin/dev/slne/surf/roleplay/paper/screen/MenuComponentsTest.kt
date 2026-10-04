package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ContextMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuCheckboxItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuLabel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuRadioGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuRadioItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuSub
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuSubTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Menubar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenubarMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenubarTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.ContextMenuNode
import dev.slne.surf.roleplay.protocol.screen.DropdownMenuNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.MenuCheckboxItemNode
import dev.slne.surf.roleplay.protocol.screen.MenuContentNode
import dev.slne.surf.roleplay.protocol.screen.MenuGroupNode
import dev.slne.surf.roleplay.protocol.screen.MenuItemNode
import dev.slne.surf.roleplay.protocol.screen.MenuLabelNode
import dev.slne.surf.roleplay.protocol.screen.MenuRadioGroupNode
import dev.slne.surf.roleplay.protocol.screen.MenuSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.MenuSubNode
import dev.slne.surf.roleplay.protocol.screen.MenuSubTriggerNode
import dev.slne.surf.roleplay.protocol.screen.MenubarMenuNode
import dev.slne.surf.roleplay.protocol.screen.MenubarNode
import dev.slne.surf.roleplay.protocol.screen.MenubarTriggerNode
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for dropdown menus, context menus and menubars in the API and on Paper.
 */
class MenuComponentsTest {

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
     * The clicks the menu entries reported, as the entry id and the value it carried.
     */
    private val clicks = mutableListOf<String>()

    /**
     * Opens a screen with a dropdown menu, a context menu and a menubar.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        Screen(Component.text("Menüs")) {
            Column(id = "root") {
                DropdownMenu(id = "menu") {
                    Button(Component.text("Menü"), submitsInput = false, id = "trigger")
                    MenuContent(id = "content") {
                        MenuLabel(Component.text("Konto"), id = "label")
                        MenuGroup(id = "group") {
                            MenuItem(Component.text("Profil"), icon = "user", shortcut = Component.text("P"), id = "profile") { clicks += it.buttonId }
                            MenuItem(Component.text("Gesperrt"), enabled = false, id = "locked") { clicks += it.buttonId }
                        }
                        MenuSeparator(id = "sep")
                        MenuCheckboxItem(Component.text("Status"), id = "status") { clicks += "status=${it.values.checked("status")}" }
                        MenuRadioGroup("top", onSelect = { clicks += "position=${it.values.text("position")}" }, id = "position") {
                            MenuRadioItem(Component.text("Oben"), "top", id = "top")
                            MenuRadioItem(Component.text("Unten"), "bottom", id = "bottom")
                            MenuRadioItem(Component.text("Weg"), "hidden", enabled = false, id = "hidden")
                        }
                        MenuSub(id = "more") {
                            MenuSubTrigger(Component.text("Mehr"), id = "more_trigger")
                            MenuContent(id = "more_content") { MenuItem(Component.text("Teilen"), destructive = true, id = "share") }
                        }
                    }
                }
                ContextMenu(id = "ctx") {
                    Label(Component.text("Rechtsklick"), id = "area")
                    MenuContent(id = "ctx_content") { MenuItem(Component.text("Kopieren"), id = "copy") }
                }
                Menubar(id = "bar") {
                    MenubarMenu(id = "file") {
                        MenubarTrigger(Component.text("Datei"), id = "file_trigger")
                        MenuContent(id = "file_content") { MenuItem(Component.text("Neu"), id = "new") }
                    }
                }
            }
        },
        null,
    ).sessionId

    /**
     * Returns the input values the client sends with an action on the menu.
     *
     * @param status the checkbox value
     * @param position the radio value
     * @return the values
     */
    private fun values(status: String = "false", position: String = "top") = listOf(
        InputValue("menu", "true"), InputValue("status", status), InputValue("position", position), InputValue("more", "false"),
        InputValue("ctx", "false"), InputValue("file", "false"),
    )

    /**
     * Verifies that every menu part maps to its node with its settings.
     */
    @Test
    fun `menus map to their nodes`() {
        open()

        val root = assertIs<ColumnNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        val content = assertIs<MenuContentNode>(assertIs<DropdownMenuNode>(root.children[0]).children[1])
        assertIs<MenuLabelNode>(content.children[0])
        val profile = assertIs<MenuItemNode>(assertIs<MenuGroupNode>(content.children[1]).children[0])
        assertEquals("user", profile.icon)
        assertTrue(profile.shortcut != null)
        assertIs<MenuSeparatorNode>(content.children[2])
        assertIs<MenuCheckboxItemNode>(content.children[3])
        assertEquals("top", assertIs<MenuRadioGroupNode>(content.children[4]).value)
        val sub = assertIs<MenuSubNode>(content.children[5])
        assertIs<MenuSubTriggerNode>(sub.children[0])
        assertTrue(assertIs<MenuItemNode>(assertIs<MenuContentNode>(sub.children[1]).children[0]).destructive)
        assertIs<ContextMenuNode>(root.children[1])
        val file = assertIs<MenubarMenuNode>(assertIs<MenubarNode>(root.children[2]).children[0])
        assertIs<MenubarTriggerNode>(file.children[0])
    }

    /**
     * Verifies that choosing an item runs its handler, and that a disabled item is rejected.
     */
    @Test
    fun `items fire actions`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "profile", values())))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleWidgetAction(ScreenWidgetAction(session, "locked", values())))
        assertEquals(listOf("profile"), clicks)
    }

    /**
     * Verifies that a checkbox item's handler reads the new state.
     */
    @Test
    fun `checkbox items carry their state`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "status", values(status = "true"))))
        assertEquals(listOf("status=true"), clicks)
    }

    /**
     * Verifies that a radio group accepts only the values of its enabled items.
     */
    @Test
    fun `radio groups validate their value`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "position", values(position = "bottom"))))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleWidgetAction(ScreenWidgetAction(session, "position", values(position = "hidden"))))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleWidgetAction(ScreenWidgetAction(session, "position", values(position = "left"))))
        assertEquals(listOf("position=bottom"), clicks)
    }
}

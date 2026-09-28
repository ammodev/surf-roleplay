package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.EmptyMediaVariant
import dev.slne.surf.roleplay.api.client.common.screen.ItemMediaVariant
import dev.slne.surf.roleplay.api.client.common.screen.ItemSize
import dev.slne.surf.roleplay.api.client.common.screen.ItemVariant
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.empty
import dev.slne.surf.roleplay.api.client.common.screen.emptyContent
import dev.slne.surf.roleplay.api.client.common.screen.emptyDescription
import dev.slne.surf.roleplay.api.client.common.screen.emptyHeader
import dev.slne.surf.roleplay.api.client.common.screen.emptyMedia
import dev.slne.surf.roleplay.api.client.common.screen.emptyTitle
import dev.slne.surf.roleplay.api.client.common.screen.item
import dev.slne.surf.roleplay.api.client.common.screen.itemActions
import dev.slne.surf.roleplay.api.client.common.screen.itemContent
import dev.slne.surf.roleplay.api.client.common.screen.itemDescription
import dev.slne.surf.roleplay.api.client.common.screen.itemFooter
import dev.slne.surf.roleplay.api.client.common.screen.itemGroup
import dev.slne.surf.roleplay.api.client.common.screen.itemHeader
import dev.slne.surf.roleplay.api.client.common.screen.itemMedia
import dev.slne.surf.roleplay.api.client.common.screen.itemSeparator
import dev.slne.surf.roleplay.api.client.common.screen.itemTitle
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.common.screen.text
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.EmptyContentNode
import dev.slne.surf.roleplay.protocol.screen.EmptyHeaderNode
import dev.slne.surf.roleplay.protocol.screen.EmptyMediaNode
import dev.slne.surf.roleplay.protocol.screen.EmptyNode
import dev.slne.surf.roleplay.protocol.screen.ItemActionsNode
import dev.slne.surf.roleplay.protocol.screen.ItemContentNode
import dev.slne.surf.roleplay.protocol.screen.ItemFooterNode
import dev.slne.surf.roleplay.protocol.screen.ItemGroupNode
import dev.slne.surf.roleplay.protocol.screen.ItemHeaderNode
import dev.slne.surf.roleplay.protocol.screen.ItemMediaNode
import dev.slne.surf.roleplay.protocol.screen.ItemNode
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.SeparatorNode
import dev.slne.surf.roleplay.protocol.screen.TextNode
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import dev.slne.surf.roleplay.protocol.screen.EmptyMediaVariant as NodeEmptyMediaVariant
import dev.slne.surf.roleplay.protocol.screen.ItemMediaVariant as NodeItemMediaVariant
import dev.slne.surf.roleplay.protocol.screen.ItemSize as NodeItemSize
import dev.slne.surf.roleplay.protocol.screen.ItemVariant as NodeItemVariant
import dev.slne.surf.roleplay.protocol.screen.TextKind as NodeTextKind

/**
 * Tests for empty states and items in the API and on Paper.
 */
class ItemComponentsTest {

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
     * The clicks the clickable item reported.
     */
    private val clicks = mutableListOf<ScreenClick>()

    /**
     * Opens a screen with an empty state and an item group.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        screen(Component.text("Liste")) {
            column("root") {
                empty("empty", outline = true) {
                    emptyHeader("empty_header") {
                        emptyMedia("empty_media", EmptyMediaVariant.ICON, icon = "folder")
                        emptyTitle("empty_title", Component.text("Keine Akten"))
                        emptyDescription("empty_description", Component.text("Lege eine an."))
                    }
                    emptyContent("empty_content") { button("create", Component.text("Neu")) }
                }
                itemGroup("group") {
                    item("clickable", ItemVariant.OUTLINE, ItemSize.SM, onClick = { clicks += it }) {
                        itemHeader("header") { text("header_text", Component.text("Kopf")) }
                        itemMedia("media", ItemMediaVariant.ICON, icon = "user")
                        itemContent("content") {
                            itemTitle("title", Component.text("Titel"))
                            itemDescription("description", Component.text("Beschreibung"))
                        }
                        itemActions("actions") { button("open", Component.text("Öffnen")) }
                        itemFooter("footer") { text("footer_text", Component.text("Fuß")) }
                    }
                    itemSeparator("separator")
                    item("plain") { itemContent("plain_content") { itemTitle("plain_title", Component.text("Einfach")) } }
                }
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that empty states and items map to their nodes with all their settings.
     */
    @Test
    fun `empty states and items map to their nodes`() {
        open()

        val root = assertIs<ColumnNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        val empty = assertIs<EmptyNode>(root.children[0])
        assertEquals(true, empty.outline)
        val header = assertIs<EmptyHeaderNode>(empty.children[0])
        val media = assertIs<EmptyMediaNode>(header.children[0])
        assertEquals(NodeEmptyMediaVariant.ICON, media.variant)
        assertEquals("folder", media.icon)
        assertEquals(NodeTextKind.EMPTY_TITLE, assertIs<TextNode>(header.children[1]).kind)
        assertEquals(NodeTextKind.EMPTY_DESCRIPTION, assertIs<TextNode>(header.children[2]).kind)
        assertIs<EmptyContentNode>(empty.children[1])
        val group = assertIs<ItemGroupNode>(root.children[1])
        val item = assertIs<ItemNode>(group.children[0])
        assertEquals(NodeItemVariant.OUTLINE, item.variant)
        assertEquals(NodeItemSize.SM, item.size)
        assertTrue(item.clickable)
        assertIs<ItemHeaderNode>(item.children[0])
        assertEquals(NodeItemMediaVariant.ICON, assertIs<ItemMediaNode>(item.children[1]).variant)
        val content = assertIs<ItemContentNode>(item.children[2])
        assertEquals(NodeTextKind.ITEM_DESCRIPTION, assertIs<TextNode>(content.children[1]).kind)
        assertIs<ItemActionsNode>(item.children[3])
        assertIs<ItemFooterNode>(item.children[4])
        assertIs<SeparatorNode>(group.children[1])
        assertEquals(false, assertIs<ItemNode>(group.children[2]).clickable)
    }

    /**
     * Verifies that a click on a clickable item runs its handler.
     */
    @Test
    fun `clickable items fire actions`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "clickable")))
        assertEquals(1, clicks.size)
    }

    /**
     * Verifies that an action for an item that cannot be clicked is rejected.
     */
    @Test
    fun `plain items reject actions`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleWidgetAction(ScreenWidgetAction(session, "plain")))
    }
}

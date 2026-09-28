package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.BadgeElement
import dev.slne.surf.roleplay.api.client.common.screen.BadgeVariant
import dev.slne.surf.roleplay.api.client.common.screen.KbdElement
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.badge
import dev.slne.surf.roleplay.api.client.common.screen.kbd
import dev.slne.surf.roleplay.api.client.common.screen.kbdGroup
import dev.slne.surf.roleplay.api.client.common.screen.separator
import dev.slne.surf.roleplay.api.client.common.screen.text
import dev.slne.surf.roleplay.api.client.common.screen.textList
import dev.slne.surf.roleplay.protocol.screen.BadgeNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.KbdGroupNode
import dev.slne.surf.roleplay.protocol.screen.KbdNode
import dev.slne.surf.roleplay.protocol.screen.SeparatorNode
import dev.slne.surf.roleplay.protocol.screen.SizeMode
import dev.slne.surf.roleplay.protocol.screen.TextListNode
import dev.slne.surf.roleplay.protocol.screen.TextNode
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.IconTint
import dev.slne.surf.roleplay.api.client.common.screen.aspectRatio
import dev.slne.surf.roleplay.api.client.common.screen.skeleton
import dev.slne.surf.roleplay.api.client.common.screen.spinner
import dev.slne.surf.roleplay.protocol.screen.AspectRatioNode
import dev.slne.surf.roleplay.protocol.screen.IconColor
import dev.slne.surf.roleplay.protocol.screen.ImageNode
import dev.slne.surf.roleplay.protocol.screen.SkeletonNode
import dev.slne.surf.roleplay.protocol.screen.SpinnerNode
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSize
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSource
import dev.slne.surf.roleplay.api.client.common.screen.avatar
import dev.slne.surf.roleplay.api.client.common.screen.avatarGroup
import dev.slne.surf.roleplay.api.client.common.screen.avatarGroupCount
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupCountNode
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupNode
import dev.slne.surf.roleplay.protocol.screen.AvatarNode
import dev.slne.surf.roleplay.api.client.common.screen.AlertVariant
import dev.slne.surf.roleplay.api.client.common.screen.alert
import dev.slne.surf.roleplay.api.client.common.screen.alertDescription
import dev.slne.surf.roleplay.api.client.common.screen.alertTitle
import dev.slne.surf.roleplay.api.client.common.screen.card
import dev.slne.surf.roleplay.api.client.common.screen.cardAction
import dev.slne.surf.roleplay.api.client.common.screen.cardContent
import dev.slne.surf.roleplay.api.client.common.screen.cardDescription
import dev.slne.surf.roleplay.api.client.common.screen.cardFooter
import dev.slne.surf.roleplay.api.client.common.screen.cardHeader
import dev.slne.surf.roleplay.api.client.common.screen.cardTitle
import dev.slne.surf.roleplay.protocol.screen.AlertNode
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CardActionNode
import dev.slne.surf.roleplay.protocol.screen.CardContentNode
import dev.slne.surf.roleplay.protocol.screen.CardFooterNode
import dev.slne.surf.roleplay.protocol.screen.CardHeaderNode
import dev.slne.surf.roleplay.protocol.screen.CardNode
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import dev.slne.surf.roleplay.api.client.common.screen.screen as buildScreen
import dev.slne.surf.roleplay.protocol.screen.AlertVariant as NodeAlertVariant
import dev.slne.surf.roleplay.protocol.screen.Align as NodeAlign
import dev.slne.surf.roleplay.protocol.screen.AvatarSize as NodeAvatarSize
import dev.slne.surf.roleplay.protocol.screen.BadgeVariant as NodeBadgeVariant
import dev.slne.surf.roleplay.protocol.screen.Orientation as NodeOrientation
import dev.slne.surf.roleplay.protocol.screen.TextKind as NodeTextKind

/**
 * Tests for the display components in the API and on Paper.
 */
class DisplayComponentsTest {

    /**
     * Maps the root of a screen built with the DSL.
     *
     * @param build the builder of the root
     * @return the mapped root node
     */
    private fun map(build: dev.slne.surf.roleplay.api.client.common.screen.ElementsBuilder.() -> Unit) =
        ScreenMapper.toNode(buildScreen(Component.text("Anzeige")) { column("root") { build() } }.root)

    /**
     * Verifies that texts, lists, separators, keys and badges map to their nodes with all their
     * settings, and that separators grow along their orientation.
     */
    @Test
    fun `texts, separators, keys and badges map to their nodes`() {
        val root = assertIs<ColumnNode>(
            map {
                text("title", Component.text("Titel"), kind = TextKind.H2, maxLines = 1, align = Alignment.CENTER)
                textList("list", listOf(Component.text("Eins"), Component.text("Zwei")), ordered = true)
                separator("line")
                separator("upright", orientation = Orientation.VERTICAL)
                kbdGroup("keys") {
                    kbd("ctrl", Component.text("Strg"))
                    kbd("cmd", icon = "command")
                }
                badge("badge", Component.text("Neu"), icon = "check", variant = BadgeVariant.DESTRUCTIVE)
            },
        )

        val text = assertIs<TextNode>(root.children[0])
        assertEquals(NodeTextKind.H2, text.kind)
        assertEquals(1, text.maxLines)
        assertEquals(NodeAlign.CENTER, text.align)
        assertEquals(2, assertIs<TextListNode>(root.children[1]).items.size)
        val line = assertIs<SeparatorNode>(root.children[2])
        assertEquals(SizeMode.GROW, line.width.mode)
        val upright = assertIs<SeparatorNode>(root.children[3])
        assertEquals(NodeOrientation.VERTICAL, upright.orientation)
        assertEquals(SizeMode.GROW, upright.height.mode)
        val keys = assertIs<KbdGroupNode>(root.children[4])
        assertEquals("command", assertIs<KbdNode>(keys.children[1]).icon)
        val badge = assertIs<BadgeNode>(root.children[5])
        assertEquals(NodeBadgeVariant.DESTRUCTIVE, badge.variant)
        assertEquals("check", badge.icon)
    }

    /**
     * Verifies that the texts of texts, keys and badges can be changed by patches.
     */
    @Test
    fun `display texts can be patched`() {
        val text = TextElement("t", Component.text("a"))
        val key = KbdElement("k", Component.text("b"))
        val badge = BadgeElement("b", Component.text("c"))

        assertEquals(Component.text("Z"), (ElementRules.rule(text)!!.withText(text, Component.text("Z")) as TextElement).text)
        assertEquals(Component.text("Z"), (ElementRules.rule(key)!!.withText(key, Component.text("Z")) as KbdElement).text)
        assertEquals(Component.text("Z"), (ElementRules.rule(badge)!!.withText(badge, Component.text("Z")) as BadgeElement).text)
    }

    /**
     * Verifies that skeletons, spinners and aspect ratio boxes map to their nodes, and that an
     * aspect ratio box grows by default.
     */
    @Test
    fun `loading components map to their nodes`() {
        val root = assertIs<ColumnNode>(
            map {
                skeleton("skeleton", ElementSize.fixed(40), ElementSize.fixed(8), round = true)
                spinner("spinner", size = 14, tint = IconTint.MUTED)
                aspectRatio("ratio", 16f / 9f) {
                    image("image", Key.key("minecraft", "textures/block/stone.png"))
                }
            },
        )

        val skeleton = assertIs<SkeletonNode>(root.children[0])
        assertEquals(40, skeleton.width.value)
        assertEquals(true, skeleton.round)
        val spinner = assertIs<SpinnerNode>(root.children[1])
        assertEquals(14, spinner.size)
        assertEquals(IconColor.MUTED, spinner.color)
        val ratio = assertIs<AspectRatioNode>(root.children[2])
        assertEquals(16f / 9f, ratio.ratio)
        assertEquals(SizeMode.GROW, ratio.width.mode)
        assertIs<ImageNode>(ratio.children.single())
    }

    /**
     * Verifies that avatars map their player, texture and fallback sources, and that groups hold
     * avatars and a count.
     */
    @Test
    fun `avatars map to their nodes`() {
        val player = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
        val root = assertIs<ColumnNode>(
            map {
                avatarGroup("group") {
                    avatar("player", Component.text("NO"), AvatarSource.Player(player), AvatarSize.LG, badgeIcon = "check")
                    avatar("texture", Component.text("AP"), AvatarSource.Texture(Key.key("minecraft", "textures/item/apple.png")), AvatarSize.SM)
                    avatar("fallback", Component.text("CN"))
                    avatarGroupCount("count", Component.text("+3"))
                }
            },
        )

        val group = assertIs<AvatarGroupNode>(root.children.single())
        val playerNode = assertIs<AvatarNode>(group.children[0])
        assertEquals(player.toString(), playerNode.playerId)
        assertEquals(NodeAvatarSize.LG, playerNode.size)
        assertEquals(true, playerNode.badge)
        assertEquals("check", playerNode.badgeIcon)
        val textureNode = assertIs<AvatarNode>(group.children[1])
        assertEquals("minecraft:textures/item/apple.png", textureNode.texture)
        assertEquals(null, textureNode.playerId)
        val fallbackNode = assertIs<AvatarNode>(group.children[2])
        assertEquals(null, fallbackNode.texture)
        assertIs<AvatarGroupCountNode>(group.children[3])
    }

    /**
     * Verifies that alerts and cards map to their nodes, and that alerts grow by default.
     */
    @Test
    fun `alerts and cards map to their nodes`() {
        val root = assertIs<ColumnNode>(
            map {
                alert("alert", AlertVariant.DESTRUCTIVE, icon = "circle-alert") {
                    alertTitle("alert_title", Component.text("Fehler"))
                    alertDescription("alert_description", Component.text("Etwas ging schief."))
                }
                card("card") {
                    cardHeader("header") {
                        cardTitle("title", Component.text("Konto"))
                        cardDescription("description", Component.text("Deine Daten"))
                        cardAction("action") { button("more", Component.text("Mehr")) }
                    }
                    cardContent("content") { text("body", Component.text("Inhalt")) }
                    cardFooter("footer") { button("save", Component.text("Speichern")) }
                }
            },
        )

        val alert = assertIs<AlertNode>(root.children[0])
        assertEquals(NodeAlertVariant.DESTRUCTIVE, alert.variant)
        assertEquals("circle-alert", alert.icon)
        assertEquals(SizeMode.GROW, alert.width.mode)
        assertEquals(NodeTextKind.ALERT_TITLE, assertIs<TextNode>(alert.children[0]).kind)
        assertEquals(NodeTextKind.ALERT_DESCRIPTION, assertIs<TextNode>(alert.children[1]).kind)
        val card = assertIs<CardNode>(root.children[1])
        val header = assertIs<CardHeaderNode>(card.children[0])
        assertEquals(NodeTextKind.CARD_TITLE, assertIs<TextNode>(header.children[0]).kind)
        assertEquals(NodeTextKind.CARD_DESCRIPTION, assertIs<TextNode>(header.children[1]).kind)
        assertIs<ButtonNode>(assertIs<CardActionNode>(header.children[2]).children.single())
        assertIs<CardContentNode>(card.children[1])
        assertIs<CardFooterNode>(card.children[2])
    }
}

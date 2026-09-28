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
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import dev.slne.surf.roleplay.api.client.common.screen.screen as buildScreen
import dev.slne.surf.roleplay.protocol.screen.Align as NodeAlign
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
}

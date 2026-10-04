package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ImageElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.ProgressElement
import dev.slne.surf.roleplay.api.client.common.screen.RowElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScrollListElement
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.SelectElement
import dev.slne.surf.roleplay.api.client.common.screen.Spacing
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CheckboxNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.ImageNode
import dev.slne.surf.roleplay.protocol.screen.InsertNode
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.NumberInputNode
import dev.slne.surf.roleplay.protocol.screen.ProgressNode
import dev.slne.surf.roleplay.protocol.screen.RemoveNode
import dev.slne.surf.roleplay.protocol.screen.ReplaceNode
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScrollListNode
import dev.slne.surf.roleplay.protocol.screen.SelectGroup
import dev.slne.surf.roleplay.protocol.screen.SelectNode
import dev.slne.surf.roleplay.protocol.screen.SelectOption
import dev.slne.surf.roleplay.protocol.screen.SetEnabled
import dev.slne.surf.roleplay.protocol.screen.SetProgress
import dev.slne.surf.roleplay.protocol.screen.SetText
import dev.slne.surf.roleplay.protocol.screen.SetValue
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [ScreenMapper].
 */
class ScreenMapperTest {

    /**
     * Returns the component JSON of a plain text.
     *
     * @param text the text
     * @return the JSON
     */
    private fun json(text: String) = ScreenMapper.text(Component.text(text))

    /**
     * Verifies that texts become component JSON that keeps their style.
     */
    @Test
    fun `texts become component json`() {
        assertEquals("""{"color":"red","text":"Hallo"}""", ScreenMapper.text(Component.text("Hallo", NamedTextColor.RED)))
    }

    /**
     * Verifies that every element kind becomes its node with the same settings.
     */
    @Test
    fun `every element becomes its node`() {
        val element = ColumnElement(
            "root",
            width = ElementSize.fixed(200),
            height = ElementSize.grow(2),
            gap = 4,
            padding = Spacing(1, 2, 3, 4),
            mainAlign = Alignment.CENTER,
            crossAlign = Alignment.STRETCH,
            children = listOf(
                RowElement("row", gap = 1, children = listOf(LabelElement("label", Component.text("A")))),
                ScrollListElement("list", gap = 2, children = listOf(ButtonElement("button", Component.text("B"), enabled = false))),
                TextInputElement("name", "Max", Component.text("Name"), 16, required = true, enabled = false),
                NumberInputElement("age", 30, 18, 99, required = true),
                CheckboxElement("agree", Component.text("Ja"), checked = true),
                SelectElement("city", listOf(SelectChoiceGroup(null, listOf(SelectChoice("north", Component.text("Nord"))))), "north", required = true),
                ImageElement("logo", Key.key("surf-roleplay", "textures/gui/logo.png")),
                ProgressElement("load", 0.5f, Component.text("50")),
            ),
        )

        val expected = ColumnNode(
            "root",
            width = Sizing.fixed(200),
            height = Sizing.grow(2),
            gap = 4,
            padding = Insets(1, 2, 3, 4),
            mainAlign = Align.CENTER,
            crossAlign = Align.STRETCH,
            children = listOf(
                RowNode("row", gap = 1, children = listOf(LabelNode("label", text = json("A")))),
                ScrollListNode("list", gap = 2, children = listOf(ButtonNode("button", text = json("B"), enabled = false))),
                TextInputNode("name", value = "Max", placeholder = json("Name"), maxLength = 16, required = true, enabled = false),
                NumberInputNode("age", value = 30, min = 18, max = 99, required = true),
                CheckboxNode("agree", label = json("Ja"), checked = true),
                SelectNode("city", groups = listOf(SelectGroup(options = listOf(SelectOption("north", json("Nord"))))), selected = "north", placeholder = ScreenMapper.text(Component.empty()), required = true),
                ImageNode("logo", texture = "surf-roleplay:textures/gui/logo.png"),
                ProgressNode("load", progress = 0.5f, label = json("50")),
            ),
        )
        assertEquals(expected, ScreenMapper.toNode(element))
    }

    /**
     * Verifies that every change becomes its patch operation.
     */
    @Test
    fun `every change becomes its operation`() {
        val changes = listOf(
            ScreenChange.Replace("a", LabelElement("a", Component.text("X"))),
            ScreenChange.Insert("list", 2, LabelElement("b", Component.text("Y"))),
            ScreenChange.Remove("c"),
            ScreenChange.SetText("d", Component.text("Z")),
            ScreenChange.SetValue("e", "v"),
            ScreenChange.SetProgress("f", 0.25f),
            ScreenChange.SetEnabled("g", false),
        )

        assertEquals(
            listOf(
                ReplaceNode("a", LabelNode("a", text = json("X"))),
                InsertNode("list", 2, LabelNode("b", text = json("Y"))),
                RemoveNode("c"),
                SetText("d", json("Z")),
                SetValue("e", "v"),
                SetProgress("f", 0.25f),
                SetEnabled("g", false),
            ),
            changes.map { ScreenMapper.toOperation(it) },
        )
    }
}

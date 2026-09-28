package dev.slne.surf.roleplay.fabric.ui.dev

import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.fabric.ui.RoleplayScreenHost
import dev.slne.surf.roleplay.fabric.ui.ScreenHostListener
import dev.slne.surf.roleplay.fabric.ui.widget.ButtonWidget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CheckboxNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.DropdownNode
import dev.slne.surf.roleplay.protocol.screen.DropdownOption
import dev.slne.surf.roleplay.protocol.screen.ImageNode
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.NumberInputNode
import dev.slne.surf.roleplay.protocol.screen.ProgressNode
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScrollListNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.minecraft.client.Minecraft

/**
 * A development-only client command, `/rpui`, that opens a hardcoded screen with every widget
 * kind to check the toolkit's rendering without a server.
 */
object DevScreenCommand {

    /**
     * Registers the command.
     */
    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommands.literal("rpui").executes {
                    val minecraft = Minecraft.getInstance()
                    minecraft.execute { minecraft.gui.setScreen(createScreen()) }
                    1
                },
            )
        }
    }

    /**
     * Creates the demo screen, which logs the input values of every button click.
     *
     * @return the screen
     */
    private fun createScreen(): RoleplayScreenHost {
        val listener = object : ScreenHostListener {
            /**
             * Logs the clicked button and the input values.
             *
             * @param host the screen
             * @param button the clicked button
             */
            override fun buttonClicked(host: RoleplayScreenHost, button: ButtonWidget) {
                RoleplayClient.log.info("rpui: {} clicked with {}", button.id, host.inputValues())
            }

            /**
             * Closes the screen.
             *
             * @param host the screen
             */
            override fun closeRequested(host: RoleplayScreenHost) {
                Minecraft.getInstance().gui.setScreen(null)
            }
        }
        return RoleplayScreenHost("""{"text":"Toolkit-Test"}""", WidgetFactory.create(tree()), true, listener)
    }

    /**
     * Builds the demo tree.
     *
     * @return the root node
     */
    private fun tree() = ColumnNode(
        id = "root",
        width = Sizing.fixed(320),
        gap = 6,
        crossAlign = Align.STRETCH,
        children = listOf(
            LabelNode("title", text = """{"text":"Alle Widgets","color":"gold","bold":true}"""),
            RowNode(
                id = "name_row",
                gap = 6,
                crossAlign = Align.CENTER,
                children = listOf(
                    LabelNode("name_label", width = Sizing.fixed(60), text = """{"text":"Name"}"""),
                    TextInputNode("name", width = Sizing.grow(), placeholder = """{"text":"Max Mustermann"}""", maxLength = 24, required = true),
                ),
            ),
            RowNode(
                id = "age_row",
                gap = 6,
                crossAlign = Align.CENTER,
                children = listOf(
                    LabelNode("age_label", width = Sizing.fixed(60), text = """{"text":"Alter"}"""),
                    NumberInputNode("age", width = Sizing.grow(), min = 18, max = 99, required = true),
                ),
            ),
            RowNode(
                id = "city_row",
                gap = 6,
                crossAlign = Align.CENTER,
                children = listOf(
                    LabelNode("city_label", width = Sizing.fixed(60), text = """{"text":"Stadt"}"""),
                    DropdownNode(
                        "city",
                        width = Sizing.grow(),
                        options = listOf(
                            DropdownOption("north", """{"text":"Nordhafen"}"""),
                            DropdownOption("south", """{"text":"Südstadt"}"""),
                            DropdownOption("old", """{"text":"Altstadt"}"""),
                        ),
                        required = true,
                    ),
                ),
            ),
            CheckboxNode("agree", label = """{"text":"Ich akzeptiere die Regeln"}"""),
            ScrollListNode(
                id = "list",
                height = Sizing.fixed(60),
                gap = 2,
                children = List(8) { index ->
                    RowNode(
                        id = "row_$index",
                        gap = 4,
                        crossAlign = Align.CENTER,
                        children = listOf(
                            LabelNode("row_${index}_label", width = Sizing.grow(), text = """{"text":"Eintrag ${index + 1}"}"""),
                            ButtonNode("row_${index}_open", text = """{"text":"Öffnen"}"""),
                        ),
                    )
                },
            ),
            RowNode(
                id = "media",
                gap = 6,
                crossAlign = Align.CENTER,
                children = listOf(
                    ImageNode("logo", width = Sizing.fixed(16), height = Sizing.fixed(16), texture = "minecraft:textures/item/diamond.png"),
                    ProgressNode("load", width = Sizing.grow(), progress = 0.6f, label = """{"text":"60 %"}"""),
                ),
            ),
            RowNode(
                id = "buttons",
                gap = 6,
                padding = Insets(top = 4),
                mainAlign = Align.END,
                children = listOf(
                    ButtonNode("disabled", text = """{"text":"Gesperrt"}""", enabled = false),
                    ButtonNode("submit", text = """{"text":"Absenden"}"""),
                ),
            ),
        ),
    )
}

package dev.slne.surf.roleplay.fabric.settings

import com.google.gson.JsonObject
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.BadgeNode
import dev.slne.surf.roleplay.protocol.screen.BadgeVariant
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ButtonSize
import dev.slne.surf.roleplay.protocol.screen.ButtonVariant
import dev.slne.surf.roleplay.protocol.screen.CardContentNode
import dev.slne.surf.roleplay.protocol.screen.CardNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.SelectGroup
import dev.slne.surf.roleplay.protocol.screen.SelectNode
import dev.slne.surf.roleplay.protocol.screen.SelectOption
import dev.slne.surf.roleplay.protocol.screen.SeparatorNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextKind
import dev.slne.surf.roleplay.protocol.screen.TextNode

/**
 * Builds the node tree of the roleplay settings screen.
 *
 * The tree is one card with the key bindings of the roleplay category and the cursor key mode;
 * the screen title is shown by the panel, not by the tree.
 * Its interactive nodes have stable ids: `binding_<id>` is the key button of a binding,
 * `reset_<id>` restores its default key, `conflict_<id>` marks a binding whose key is also bound
 * elsewhere, `reset_all` restores every default key, and `cursor_mode` is the select of the
 * cursor key mode with the values of [KeyMode].
 */
object SettingsView {

    /**
     * The title of the settings screen, shown in the panel's title bar.
     */
    const val TITLE: String = "Roleplay-Einstellungen"

    /**
     * The id of the button that restores every default key.
     */
    const val RESET_ALL_ID: String = "reset_all"

    /**
     * The id of the select of the cursor key mode.
     */
    const val CURSOR_MODE_ID: String = "cursor_mode"

    /**
     * The prefix of the id of a binding's key button.
     */
    const val BINDING_PREFIX: String = "binding_"

    /**
     * The prefix of the id of a binding's reset button.
     */
    const val RESET_PREFIX: String = "reset_"

    /**
     * The prefix of the id of a binding's conflict badge.
     */
    const val CONFLICT_PREFIX: String = "conflict_"

    /**
     * The width of a key button, in GUI pixels.
     */
    private const val KEY_BUTTON_WIDTH = 100

    /**
     * The vertical gap between binding rows, in GUI pixels.
     */
    private const val ROW_GAP = 6

    /**
     * The width of the card, in GUI pixels.
     */
    private const val CARD_WIDTH = 380

    /**
     * Builds the settings tree.
     *
     * @param rows the key bindings to show, in display order
     * @param conflicts the ids of the bindings whose key is also bound by another binding
     * @param capturing the id of the binding that waits for a new key, or `null`
     * @param settings the current client settings
     * @return the root node of the tree
     */
    fun build(rows: List<BindingRow>, conflicts: Set<String>, capturing: String?, settings: ClientSettings): ScreenNode =
        CardNode(
            "settings",
            width = Sizing.fixed(CARD_WIDTH),
            children = listOf(
                CardContentNode(
                    "settings_content",
                    width = Sizing.grow(),
                    children = buildList {
                        add(TextNode("bindings_heading", kind = TextKind.H4, text = text("Tastenbelegung")))
                        add(
                            ColumnNode(
                                "binding_rows",
                                width = Sizing.grow(),
                                gap = ROW_GAP,
                                children = rows.map { bindingRow(it, it.id in conflicts, it.id == capturing) },
                            ),
                        )
                        add(ButtonNode(RESET_ALL_ID, text = text("Alle zurücksetzen"), submitsInput = false, variant = ButtonVariant.OUTLINE))
                        add(SeparatorNode("cursor_separator", width = Sizing.grow()))
                        add(TextNode("cursor_heading", kind = TextKind.H4, text = text("Mauszeiger")))
                        add(cursorModeSelect(settings.cursorKeyMode))
                        add(TextNode("cursor_hint", kind = TextKind.MUTED, text = text("Gilt auch für das Zielauge.")))
                    },
                ),
            ),
        )

    /**
     * Builds the row of one binding: its name, a conflict badge if needed, its key button and
     * its reset button.
     *
     * @param row the binding
     * @param conflicting whether its key is also bound by another binding
     * @param capturing whether it waits for a new key
     * @return the row node
     */
    private fun bindingRow(row: BindingRow, conflicting: Boolean, capturing: Boolean): ScreenNode =
        RowNode(
            "row_${row.id}",
            width = Sizing.grow(),
            gap = 4,
            crossAlign = Align.CENTER,
            children = buildList {
                add(TextNode("name_${row.id}", width = Sizing.grow(), text = text(row.name)))
                if (conflicting) add(BadgeNode(CONFLICT_PREFIX + row.id, text = text("Doppelt belegt"), variant = BadgeVariant.DESTRUCTIVE))
                add(
                    ButtonNode(
                        BINDING_PREFIX + row.id,
                        width = Sizing.fixed(KEY_BUTTON_WIDTH),
                        text = text(keyCaption(row, capturing)),
                        submitsInput = false,
                        variant = if (capturing) ButtonVariant.DEFAULT else ButtonVariant.OUTLINE,
                    ),
                )
                add(
                    ButtonNode(
                        RESET_PREFIX + row.id,
                        enabled = !row.isDefault,
                        submitsInput = false,
                        icon = "rotate-ccw",
                        variant = ButtonVariant.GHOST,
                        size = ButtonSize.ICON,
                    ),
                )
            },
        )

    /**
     * Returns the caption of a binding's key button.
     *
     * @param row the binding
     * @param capturing whether it waits for a new key
     * @return the prompt while capturing, "Nicht belegt" when unbound, and the key name otherwise
     */
    private fun keyCaption(row: BindingRow, capturing: Boolean): String = when {
        capturing -> "> Taste drücken <"
        row.unbound -> "Nicht belegt"
        else -> row.keyLabel
    }

    /**
     * Builds the select of the cursor key mode, which reports each change.
     *
     * @param mode the current mode
     * @return the select node
     */
    private fun cursorModeSelect(mode: KeyMode): ScreenNode =
        SelectNode(
            CURSOR_MODE_ID,
            width = Sizing.grow(),
            groups = listOf(
                SelectGroup(
                    options = listOf(
                        SelectOption(KeyMode.HOLD.name, text("Halten")),
                        SelectOption(KeyMode.TOGGLE.name, text("Umschalten")),
                    ),
                ),
            ),
            selected = mode.name,
            notifyChange = true,
        )

    /**
     * Encodes plain text as text-component JSON.
     *
     * @param plain the text
     * @return the component JSON
     */
    internal fun text(plain: String): String = JsonObject().apply { addProperty("text", plain) }.toString()
}

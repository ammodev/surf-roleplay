package dev.slne.surf.roleplay.fabric.settings

import com.google.gson.JsonObject
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.BadgeNode
import dev.slne.surf.roleplay.protocol.screen.BadgeVariant
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ButtonSize
import dev.slne.surf.roleplay.protocol.screen.ButtonVariant
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScrollAreaNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.SelectGroup
import dev.slne.surf.roleplay.protocol.screen.SelectNode
import dev.slne.surf.roleplay.protocol.screen.SelectOption
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TabsContentNode
import dev.slne.surf.roleplay.protocol.screen.TabsListNode
import dev.slne.surf.roleplay.protocol.screen.TabsNode
import dev.slne.surf.roleplay.protocol.screen.TabsTriggerNode
import dev.slne.surf.roleplay.protocol.screen.TextKind
import dev.slne.surf.roleplay.protocol.screen.TextNode

/**
 * Builds the node tree of the roleplay settings screen.
 *
 * The tree is vertical tabs with one category per trigger ([categories]); the screen title is
 * shown by the panel, not by the tree.
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
     * The id of the tabs node, whose value is the selected category.
     */
    const val TABS_ID: String = "settings_tabs"

    /**
     * The id of the category with the key bindings.
     */
    const val CONTROLS_CATEGORY: String = "controls"

    /**
     * The id of the category with the cursor settings.
     */
    const val CURSOR_CATEGORY: String = "cursor"

    /**
     * The width of the whole settings tree, in GUI pixels.
     */
    private const val WIDTH = 390

    /**
     * The height of the whole settings tree, in GUI pixels.
     */
    private const val HEIGHT = 190

    /**
     * The width of the column of category triggers, in GUI pixels.
     */
    private const val TRIGGER_WIDTH = 100

    /**
     * The categories of the settings screen, in display order. Adding a category is adding an
     * entry here.
     */
    val categories: List<SettingsCategory> = listOf(
        SettingsCategory(CONTROLS_CATEGORY, "Steuerung", "keyboard", ::controlsContent),
        SettingsCategory(CURSOR_CATEGORY, "Mauszeiger", "mouse-pointer", ::cursorContent),
    )

    /**
     * Builds the settings tree: vertical tabs with one trigger per category on the left and the
     * content of the selected category on the right.
     *
     * @param rows the key bindings to show, in display order
     * @param conflicts the ids of the bindings whose key is also bound by another binding
     * @param capturing the id of the binding that waits for a new key, or `null`
     * @param settings the current client settings
     * @param category the id of the selected category; an unknown id selects the first one
     * @return the root node of the tree
     */
    fun build(
        rows: List<BindingRow>,
        conflicts: Set<String>,
        capturing: String?,
        settings: ClientSettings,
        category: String = categories.first().id,
    ): ScreenNode {
        val state = SettingsState(rows, conflicts, capturing, settings)
        return TabsNode(
            TABS_ID,
            width = Sizing.fixed(WIDTH),
            height = Sizing.fixed(HEIGHT),
            value = categories.firstOrNull { it.id == category }?.id ?: categories.first().id,
            orientation = Orientation.VERTICAL,
            notifyChange = true,
            children = listOf(
                TabsListNode(
                    "settings_tab_list",
                    width = Sizing.fixed(TRIGGER_WIDTH),
                    height = Sizing.grow(),
                    children = categories.map {
                        TabsTriggerNode("tab_${it.id}", width = Sizing.grow(), value = it.id, text = text(it.title), icon = it.icon)
                    },
                ),
            ) + categories.map {
                TabsContentNode(
                    "content_${it.id}",
                    width = Sizing.grow(),
                    height = Sizing.grow(),
                    value = it.id,
                    children = listOf(
                        ScrollAreaNode(
                            "scroll_${it.id}",
                            width = Sizing.grow(),
                            height = Sizing.grow(),
                            children = listOf(
                                ColumnNode("page_${it.id}", width = Sizing.grow(), gap = ROW_GAP, children = it.content(state)),
                            ),
                        ),
                    ),
                )
            },
        )
    }

    /**
     * Builds the content of the controls category: the key bindings with their conflict badges,
     * key buttons and reset buttons, and the button that restores every default key.
     *
     * @param state what the settings screen shows
     * @return the nodes of the category
     */
    private fun controlsContent(state: SettingsState): List<ScreenNode> = listOf(
        TextNode("bindings_heading", kind = TextKind.H4, text = text("Tastenbelegung")),
        ColumnNode(
            "binding_rows",
            width = Sizing.grow(),
            gap = ROW_GAP,
            children = state.rows.map { bindingRow(it, it.id in state.conflicts, it.id == state.capturing) },
        ),
        ButtonNode(RESET_ALL_ID, text = text("Alle zurücksetzen"), submitsInput = false, variant = ButtonVariant.OUTLINE),
    )

    /**
     * Builds the content of the cursor category: the cursor key mode select and its hint.
     *
     * @param state what the settings screen shows
     * @return the nodes of the category
     */
    private fun cursorContent(state: SettingsState): List<ScreenNode> = listOf(
        TextNode("cursor_heading", kind = TextKind.H4, text = text("Mauszeiger")),
        cursorModeSelect(state.settings.cursorKeyMode),
        TextNode("cursor_hint", kind = TextKind.MUTED, text = text("Gilt auch für das Zielauge.")),
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

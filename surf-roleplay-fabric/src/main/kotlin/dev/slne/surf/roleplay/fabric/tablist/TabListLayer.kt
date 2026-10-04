package dev.slne.surf.roleplay.fabric.tablist

import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.fabric.protocol.FabricPacketDispatcher
import dev.slne.surf.roleplay.fabric.server.RoleplayServerState
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.text.TextBlock
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier

/**
 * Draws the roleplay tab list: a card at the top centre of the HUD that is shown while the
 * player-list key is held on the roleplay server and no screen is open. It shows the last state
 * the server sent and counts the clock, the playtime and the restart countdown itself.
 */
object TabListLayer {

    /**
     * The last state the server sent on the current connection.
     */
    private val store: TabListStore = TabListStore { RoleplayClient.log.warn(it) }

    /**
     * The state that tells whether the current server is the roleplay server.
     */
    private lateinit var serverState: RoleplayServerState

    /**
     * Registers the tab list packet, the reset when the roleplay state ends and the HUD element.
     *
     * @param state the roleplay server state
     */
    fun register(state: RoleplayServerState) {
        serverState = state
        FabricPacketDispatcher.on(Packets.TAB_LIST_STATE) { packet ->
            Minecraft.getInstance().execute { store.receive(packet, serverState.isActive, System.nanoTime()) }
        }
        state.onChange { active -> if (!active) Minecraft.getInstance().execute { store.clear() } }
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(RoleplayClient.MOD_ID, "tab_list")) { graphics, _ -> render(graphics) }
    }

    /**
     * Draws the card if the roleplay state is active, a state was received, the player-list key
     * is held and no screen is open.
     *
     * @param graphics the GUI graphics of this frame
     */
    private fun render(graphics: GuiGraphicsExtractor) {
        val mc = Minecraft.getInstance()
        val current = store.state ?: return
        if (!serverState.isActive || mc.gui.screen() != null || !mc.options.keyPlayerList.isDown) return
        val elapsedMillis = (System.nanoTime() - store.receivedAtNanos) / 1_000_000L
        val now = TabListClock.serverNow(current.serverTimeMillis, elapsedMillis)
        val ping = mc.player?.let { mc.connection?.getPlayerInfo(it.uuid)?.latency }
        val view = TabListView.of(current, now, store.zone, ping)
        val ui = UiGraphics(graphics, mc.font, Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK))
        val x = (mc.window.guiScaledWidth - WIDTH) / 2
        val height = layout(ui, view, x, TOP, draw = false)
        val card = Rect(x, TOP, WIDTH, height)
        ui.fillRounded(card, ui.tokens.card)
        ui.borderRounded(card, ui.tokens.border)
        layout(ui, view, x, TOP, draw = true)
    }

    /**
     * Lays out the content of the card from its top edge and draws it if asked to.
     *
     * @param ui the graphics to draw and measure with
     * @param view the content
     * @param x the left edge of the card
     * @param top the top edge of the card
     * @param draw whether to draw, or only to measure
     * @return the height of the card
     */
    private fun layout(ui: UiGraphics, view: TabListView, x: Int, top: Int, draw: Boolean): Int {
        val tokens = ui.tokens
        val left = x + PADDING
        val inner = WIDTH - 2 * PADDING
        var y = top + PADDING
        if (draw) {
            ui.icon(LOGO_ICON, Rect(left, y, HEADER, HEADER), tokens.primary)
            ui.plainText(TITLE, left + HEADER + 4, y + (HEADER - ui.font.lineHeight) / 2 + 1, tokens.cardForeground)
        }
        y += HEADER + SECTION_GAP
        y = divider(ui, left, y, inner, draw)
        if (view.organisations.isNotEmpty()) {
            y = grid(ui, view.organisations, left, y, inner, draw) + SECTION_GAP
        }
        y = grid(ui, view.info, left, y, inner, draw)
        view.restart?.let {
            y += ROW_GAP
            if (draw) {
                ui.icon(RESTART_ICON, Rect(left, y, ICON, ICON), tokens.mutedForeground)
                ui.plainText(it, left + ICON + 3, y + 1, tokens.mutedForeground)
            }
            y += ui.font.lineHeight + 1
        }
        view.announcement?.let {
            y += SECTION_GAP
            val textWidth = inner - ICON - 3
            if (draw) {
                ui.icon(ANNOUNCEMENT_ICON, Rect(left, y, ICON, ICON), tokens.primary)
                ui.wrappedText(it, left + ICON + 3, y + 1, textWidth, tokens.cardForeground, maxLines = ANNOUNCEMENT_LINES)
            }
            y += TextBlock.size(ui, it, textWidth, ANNOUNCEMENT_LINES).height + 1
        }
        y += SECTION_GAP
        y = divider(ui, left, y, inner, draw)
        y = grid(ui, view.footer, left, y, inner, draw)
        return y + PADDING - top
    }

    /**
     * Draws a horizontal line across the content if asked to.
     *
     * @param ui the graphics to draw with
     * @param left the left edge of the content
     * @param y the top edge of the line
     * @param width the width of the content
     * @param draw whether to draw
     * @return the top edge of what follows the line
     */
    private fun divider(ui: UiGraphics, left: Int, y: Int, width: Int, draw: Boolean): Int {
        if (draw) ui.fill(Rect(left, y, width, 1), ui.tokens.border)
        return y + 1 + SECTION_GAP
    }

    /**
     * Lays out cells in rows of up to [COLUMNS] equal columns, each with its label above its
     * value, and draws them if asked to. Texts longer than their column end with an ellipsis.
     *
     * @param ui the graphics to draw with
     * @param cells the cells
     * @param left the left edge of the content
     * @param top the top edge of the first row
     * @param width the width of the content
     * @param draw whether to draw
     * @return the bottom edge of the last row
     */
    private fun grid(ui: UiGraphics, cells: List<TabListView.Cell>, left: Int, top: Int, width: Int, draw: Boolean): Int {
        val line = ui.font.lineHeight
        val cellHeight = 2 * line + 1
        val columnWidth = (width - (COLUMNS - 1) * COLUMN_GAP) / COLUMNS
        val rows = cells.chunked(COLUMNS)
        if (draw) {
            rows.forEachIndexed { row, rowCells ->
                rowCells.forEachIndexed { column, cell ->
                    val cellX = left + column * (columnWidth + COLUMN_GAP)
                    val cellY = top + row * (cellHeight + ROW_GAP)
                    cell(ui, cell, cellX, cellY, columnWidth)
                }
            }
        }
        return top + rows.size * cellHeight + (rows.size - 1).coerceAtLeast(0) * ROW_GAP
    }

    /**
     * Draws one cell: the optional icon and the muted label, and the value below them.
     *
     * @param ui the graphics to draw with
     * @param cell the cell
     * @param x the left edge of the cell
     * @param y the top edge of the cell
     * @param width the width of the cell
     */
    private fun cell(ui: UiGraphics, cell: TabListView.Cell, x: Int, y: Int, width: Int) {
        val tokens = ui.tokens
        var labelX = x
        if (cell.icon.isNotEmpty()) {
            ui.icon(cell.icon, Rect(x, y, ICON - 1, ICON - 1), tokens.mutedForeground)
            labelX += ICON + 2
        }
        text(ui, cell.label, labelX, y, width - (labelX - x), tokens.mutedForeground)
        text(ui, cell.value, x, y + ui.font.lineHeight + 1, width, tokens.cardForeground)
    }

    /**
     * Draws a text on one line within a width. Component JSON ends with an ellipsis if it is
     * longer; plain text is drawn directly and is shortened with an ellipsis if it is longer.
     *
     * @param ui the graphics to draw with
     * @param text the text
     * @param x the left edge
     * @param y the top edge
     * @param width the largest width
     * @param color the ARGB colour
     */
    private fun text(ui: UiGraphics, text: TabListView.Text, x: Int, y: Int, width: Int, color: Int) {
        if (text.json) {
            ui.wrappedText(text.value, x, y, width, color, maxLines = 1)
            return
        }
        val shown = if (ui.plainWidth(text.value) <= width) {
            text.value
        } else {
            ui.font.plainSubstrByWidth(text.value, (width - ui.plainWidth(ELLIPSIS)).coerceAtLeast(0)) + ELLIPSIS
        }
        ui.plainText(shown, x, y, color)
    }

    /**
     * The ellipsis that ends a shortened plain text.
     */
    private const val ELLIPSIS: String = "…"

    /**
     * The width of the card in GUI pixels.
     */
    private const val WIDTH: Int = 320

    /**
     * The distance of the card from the top of the screen.
     */
    private const val TOP: Int = 8

    /**
     * The inner space of the card.
     */
    private const val PADDING: Int = 8

    /**
     * The size of the logo icon and the height of the header.
     */
    private const val HEADER: Int = 12

    /**
     * The size of the small icons.
     */
    private const val ICON: Int = 9

    /**
     * The space between sections.
     */
    private const val SECTION_GAP: Int = 6

    /**
     * The space between rows of cells.
     */
    private const val ROW_GAP: Int = 4

    /**
     * The number of cells in one row.
     */
    private const val COLUMNS: Int = 3

    /**
     * The space between columns.
     */
    private const val COLUMN_GAP: Int = 8

    /**
     * The most lines of the announcement shown.
     */
    private const val ANNOUNCEMENT_LINES: Int = 3

    /**
     * The title in the header.
     */
    private const val TITLE: String = "Surf Roleplay"

    /**
     * The Lucide name of the logo icon.
     */
    private const val LOGO_ICON: String = "landmark"

    /**
     * The Lucide name of the restart icon.
     */
    private const val RESTART_ICON: String = "rotate-ccw"

    /**
     * The Lucide name of the announcement icon.
     */
    private const val ANNOUNCEMENT_ICON: String = "megaphone"
}

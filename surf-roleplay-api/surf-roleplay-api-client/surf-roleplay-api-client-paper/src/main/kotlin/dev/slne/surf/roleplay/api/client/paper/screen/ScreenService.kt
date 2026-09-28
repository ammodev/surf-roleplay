package dev.slne.surf.roleplay.api.client.paper.screen

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

private val service = requiredService<ScreenService>()

/**
 * Opens and closes server-driven screens for players who run the roleplay client mod.
 *
 * Each player has a stack of open screens. A screen replaces what is shown, or appears as a
 * dialog or sheet over the screens below it; only the top screen receives input. Closing the top
 * screen with Escape shows the screens below it again. Every method must be called on the
 * player's region thread.
 */
interface ScreenService {
    /**
     * Opens a generic screen for a player.
     *
     * Without a parent, every screen the player has open is closed first. With a parent, the
     * screens above the parent are closed and the new screen opens on top of it.
     *
     * @param player the player
     * @param definition the screen to open
     * @param parent the open screen of the same player to open on top of, or `null`
     * @param presentation how the screen is shown relative to the screens below it
     * @param sheetSide the window edge a [ScreenPresentation.SHEET] is attached to
     * @return the open screen
     * @throws IllegalArgumentException if [parent] is not an open screen of [player]
     */
    fun open(
        player: Player,
        definition: ScreenDefinition,
        parent: OpenScreen? = null,
        presentation: ScreenPresentation = ScreenPresentation.SCREEN,
        sheetSide: SheetSide = SheetSide.RIGHT,
    ): OpenScreen

    /**
     * Asks a player to confirm something in a dialog over a parent screen, or over the world if
     * there is no parent. The dialog uses the parent's theme.
     *
     * Once the dialog is open, exactly one handler runs: [onConfirm] when the player confirms, and
     * [onCancel] when the player cancels, closes the dialog, or the dialog closes for any other
     * reason, such as its parent closing or the player leaving. If opening fails, no handler runs.
     * When the player leaves, [onCancel] runs at a point where no further screen can be opened for
     * the player.
     *
     * @param player the player
     * @param parent the open screen to show the dialog over, or `null`
     * @param title the dialog's title
     * @param text the question
     * @param confirmLabel the caption of the confirm button
     * @param cancelLabel the caption of the cancel button
     * @param destructive whether confirming destroys something, which the dialog marks
     * @param onConfirm the handler run when the player confirms
     * @param onCancel the handler run when the dialog closes without confirmation
     * @return the open dialog
     * @throws IllegalArgumentException if [parent] is not an open screen of [player]
     */
    fun confirm(
        player: Player,
        parent: OpenScreen?,
        title: Component,
        text: Component,
        confirmLabel: Component,
        cancelLabel: Component,
        destructive: Boolean = false,
        onConfirm: () -> Unit,
        onCancel: () -> Unit = {},
    ): OpenScreen

    /**
     * Returns the screens a player has open.
     *
     * @param player the player
     * @return the open screens, from bottom to top
     */
    fun openScreens(player: Player): List<OpenScreen>

    /**
     * Closes every screen a player has open.
     *
     * @param player the player
     */
    fun closeAll(player: Player)

    /** Provides access to the single registered [ScreenService]. */
    companion object : ScreenService by service {
        /** The single registered [ScreenService] instance. */
        val INSTANCE get() = service
    }
}

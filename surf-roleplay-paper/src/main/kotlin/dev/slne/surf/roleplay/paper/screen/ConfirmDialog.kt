package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.IconTint
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.screen
import net.kyori.adventure.text.Component

/**
 * Opens confirmation dialogs: a question with a confirm and a cancel button, shown as a dialog
 * over a parent screen in the parent's theme. A destructive confirmation shows a warning icon and
 * a trash icon on its confirm button.
 */
object ConfirmDialog {

    /**
     * The id of the confirm button.
     */
    const val CONFIRM_ID: String = "confirm"

    /**
     * The id of the cancel button.
     */
    const val CANCEL_ID: String = "cancel"

    /**
     * Opens a confirmation dialog. Exactly one handler runs: [onConfirm] on confirmation, and
     * [onCancel] on cancel or when the dialog closes for any other reason.
     *
     * @param state the player's screen state
     * @param parentSessionId the session to show the dialog over, or `null`
     * @param title the dialog's title
     * @param text the question
     * @param confirmLabel the caption of the confirm button
     * @param cancelLabel the caption of the cancel button
     * @param destructive whether confirming destroys something
     * @param onConfirm the handler run on confirmation
     * @param onCancel the handler run when the dialog closes without confirmation
     * @return the open dialog
     */
    fun open(
        state: PlayerScreenState,
        parentSessionId: Int?,
        title: Component,
        text: Component,
        confirmLabel: Component,
        cancelLabel: Component,
        destructive: Boolean,
        onConfirm: () -> Unit,
        onCancel: () -> Unit,
    ): OpenScreen {
        var decided = false
        val (theme, variant) = state.themeOf(parentSessionId)
        val definition = screen(title) {
            this.theme = theme
            this.variant = variant
            onClose {
                if (!decided) {
                    decided = true
                    onCancel()
                }
            }
            column("confirm_root", gap = 10, crossAlign = Alignment.STRETCH) {
                row("confirm_body", gap = 8, crossAlign = Alignment.CENTER) {
                    if (destructive) icon("confirm_icon", "triangle-alert", size = 16, tint = IconTint.DESTRUCTIVE)
                    label("confirm_text", text)
                }
                row("confirm_buttons", gap = 6, mainAlign = Alignment.END) {
                    button(CANCEL_ID, cancelLabel, submitsInput = false) { click ->
                        decided = true
                        click.screen.close()
                        onCancel()
                    }
                    button(CONFIRM_ID, confirmLabel, submitsInput = false, icon = if (destructive) "trash" else null) { click ->
                        decided = true
                        click.screen.close()
                        onConfirm()
                    }
                }
            }
        }
        return state.open(definition, parentSessionId, ScreenPresentation.DIALOG)
    }
}

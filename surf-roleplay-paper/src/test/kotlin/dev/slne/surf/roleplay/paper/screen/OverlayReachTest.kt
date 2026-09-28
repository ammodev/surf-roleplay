package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ContextMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogContentElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuContentElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.RowElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.protocol.screen.InputValue
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertIs

/**
 * Tests that the server rejects input inside overlays the player cannot reach.
 */
class OverlayReachTest {

    /**
     * Creates a dropdown menu with one item and a sub-menu.
     *
     * @param triggerEnabled whether the trigger button is enabled
     * @param open whether the server holds the menu as open
     * @return the menu
     */
    private fun menu(triggerEnabled: Boolean, open: Boolean = false) = DropdownMenuElement(
        "menu",
        open = open,
        children = listOf(
            ButtonElement("trigger", Component.text("Menü"), enabled = triggerEnabled),
            MenuContentElement(
                "content",
                children = listOf(
                    MenuItemElement("delete", Component.text("Löschen")),
                    MenuSubElement("more", children = listOf(MenuSubTriggerElement("more_trigger"), MenuContentElement("more_content", children = listOf(MenuItemElement("share"))))),
                ),
            ),
        ),
    )

    /**
     * Validates a click in a screen made of elements.
     *
     * @param widgetId the clicked widget
     * @param values the submitted values
     * @param elements the elements of the screen
     * @return the result
     */
    private fun validate(widgetId: String, values: List<InputValue>, vararg elements: ScreenElement) =
        ScreenActionValidator.validate(ServerScreenTree(ColumnElement("root", children = elements.toList())), widgetId, values)

    /**
     * Asserts that a result is a rejection whose reason contains a text.
     *
     * @param result the result
     * @param reason the expected part of the reason
     */
    private fun assertRejected(result: ScreenActionValidator.Result, reason: String) {
        assertContains(assertIs<ScreenActionValidator.Result.Rejected>(result).reason, reason)
    }

    /**
     * Verifies that an item inside a menu whose trigger is disabled is rejected, and accepted
     * while the trigger is enabled.
     */
    @Test
    fun `items of a menu with a disabled trigger are rejected`() {
        val open = listOf(InputValue("menu", "true"))

        assertRejected(validate("delete", open, menu(triggerEnabled = false)), "cannot reach")
        assertIs<ScreenActionValidator.Result.Accepted>(validate("delete", open, menu(triggerEnabled = true)))
    }

    /**
     * Verifies that a menu the server holds as open can be used although its trigger is disabled,
     * and can be closed.
     */
    @Test
    fun `menus the server opened stay reachable`() {
        assertIs<ScreenActionValidator.Result.Accepted>(validate("delete", listOf(InputValue("menu", "true")), menu(triggerEnabled = false, open = true)))
        assertIs<ScreenActionValidator.Result.Accepted>(validate("other", listOf(InputValue("menu", "false")), menu(triggerEnabled = false, open = true), ButtonElement("other", Component.text("X"))))
    }

    /**
     * Verifies that a player cannot open a menu whose trigger is disabled.
     */
    @Test
    fun `menus with a disabled trigger cannot be opened`() {
        assertRejected(validate("other", listOf(InputValue("menu", "true")), menu(triggerEnabled = false), ButtonElement("other", Component.text("X"))), "cannot be opened")
    }

    /**
     * Verifies that a sub-menu inside an unreachable menu is unreachable too.
     */
    @Test
    fun `nested overlays inherit unreachability`() {
        assertRejected(validate("share", listOf(InputValue("menu", "true"), InputValue("more", "true")), menu(triggerEnabled = false)), "cannot reach")
    }

    /**
     * Verifies that a trigger wrapped in a container still opens its overlay.
     */
    @Test
    fun `wrapped triggers count`() {
        val dialog = DialogElement(
            "dialog",
            children = listOf(RowElement("wrap", children = listOf(ButtonElement("open", Component.text("Öffnen")))), DialogContentElement("dialog_content", children = listOf(ButtonElement("save", Component.text("Speichern"))))),
        )

        assertIs<ScreenActionValidator.Result.Accepted>(validate("save", listOf(InputValue("dialog", "true")), dialog))
    }

    /**
     * Verifies that a context menu is reachable through its area even when the area holds only
     * disabled widgets.
     */
    @Test
    fun `context menus open through their area`() {
        val context = ContextMenuElement(
            "ctx",
            children = listOf(ButtonElement("row", Component.text("Zeile"), enabled = false), MenuContentElement("ctx_content", children = listOf(MenuItemElement("copy")))),
        )

        assertIs<ScreenActionValidator.Result.Accepted>(validate("copy", listOf(InputValue("ctx", "true")), context))
    }

    /**
     * Verifies that a changed value of an input inside an unreachable dialog is rejected, and an
     * unchanged one is accepted.
     */
    @Test
    fun `inputs inside unreachable overlays keep their value`() {
        val dialog = DialogElement(
            "dialog",
            children = listOf(ButtonElement("open", Component.text("Öffnen"), enabled = false), DialogContentElement("dialog_content", children = listOf(TextInputElement("name", value = "Max")))),
        )
        val other = ButtonElement("other", Component.text("X"))

        assertRejected(validate("other", listOf(InputValue("name", "Eve")), dialog, other), "cannot reach")
        assertIs<ScreenActionValidator.Result.Accepted>(validate("other", listOf(InputValue("name", "Max")), dialog, other))
    }
}

package dev.slne.surf.roleplay.paper.handshake

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [HandshakeMessages].
 */
class HandshakeMessagesTest {

    /**
     * Renders a component as plain text.
     *
     * @param component the component to render
     * @return the plain text of [component]
     */
    private fun plain(component: net.kyori.adventure.text.Component) =
        PlainTextComponentSerializer.plainText().serialize(component)

    /**
     * Verifies that a short list of forbidden mods is listed completely.
     */
    @Test
    fun `forbidden mods message lists every mod of a short list`() {
        assertEquals(
            "Folgende Mods sind nicht erlaubt: freecam, xaerominimap. Bitte starte das Spiel über den Roleplay-Launcher.",
            plain(HandshakeMessages.forbiddenMods(listOf("freecam", "xaerominimap"))),
        )
    }

    /**
     * Verifies that a long list of forbidden mods is cut after the first mods and names how many
     * more there are.
     */
    @Test
    fun `forbidden mods message cuts a long list`() {
        val modIds = (1..15).map { "mod$it" }

        assertEquals(
            "Folgende Mods sind nicht erlaubt: mod1, mod2, mod3, mod4, mod5, mod6, mod7, mod8, mod9, mod10 " +
                    "und 5 weitere. Bitte starte das Spiel über den Roleplay-Launcher.",
            plain(HandshakeMessages.forbiddenMods(modIds)),
        )
    }
}

package dev.slne.surf.roleplay.paper.handshake

import net.kyori.adventure.text.Component

/**
 * The German disconnect messages of the mod handshake.
 */
object HandshakeMessages {

    /**
     * The message shown to a player whose client sent no hello, because it does not run the mod.
     */
    val MISSING_MOD: Component = Component.text("Bitte starte das Spiel über den Roleplay-Launcher.")

    /**
     * The message shown to a player whose mod speaks a different protocol version.
     */
    val PROTOCOL_MISMATCH: Component = Component.text("Deine Roleplay-Mod ist nicht aktuell. Update über den Launcher.")

    /**
     * Creates the message shown to a player who loaded mods that are not allowed.
     *
     * @param modIds the ids of the mods that are not allowed
     * @return the message listing the mods
     */
    fun forbiddenMods(modIds: List<String>): Component = Component.text(
        "Folgende Mods sind nicht erlaubt: ${modIds.joinToString()}. Bitte starte das Spiel über den Roleplay-Launcher."
    )

    /**
     * Returns the disconnect message for a rejected handshake.
     *
     * @param outcome the outcome of the handshake
     * @return the message, or `null` if [outcome] accepts the client
     */
    fun disconnectMessage(outcome: HandshakeOutcome): Component? = when (outcome) {
        HandshakeOutcome.Accepted -> null
        HandshakeOutcome.MissingMod -> MISSING_MOD
        is HandshakeOutcome.ProtocolMismatch -> PROTOCOL_MISMATCH
        is HandshakeOutcome.ForbiddenMods -> forbiddenMods(outcome.modIds)
    }
}

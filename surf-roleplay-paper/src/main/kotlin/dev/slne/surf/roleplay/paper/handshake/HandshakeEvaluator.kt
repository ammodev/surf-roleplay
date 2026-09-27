package dev.slne.surf.roleplay.paper.handshake

import dev.slne.surf.roleplay.protocol.PROTOCOL_VERSION
import dev.slne.surf.roleplay.protocol.packets.ClientHello

/**
 * The result of evaluating a client's handshake.
 */
sealed interface HandshakeOutcome {
    /**
     * The client may continue into the world.
     */
    data object Accepted : HandshakeOutcome

    /**
     * The client sent no hello in time, so it does not run the roleplay mod.
     */
    data object MissingMod : HandshakeOutcome

    /**
     * The client's mod speaks a different protocol version than the server.
     *
     * @property clientVersion the protocol version the client reported
     * @property serverVersion the protocol version of the server
     */
    data class ProtocolMismatch(val clientVersion: Int, val serverVersion: Int) : HandshakeOutcome

    /**
     * The client has loaded mods that are not on the whitelist.
     *
     * @property modIds the ids of the mods that are not allowed, sorted
     */
    data class ForbiddenMods(val modIds: List<String>) : HandshakeOutcome
}

/**
 * Decides whether a client may join, based on the hello it sent in the configuration phase.
 *
 * A client may join if it speaks the server's protocol version and every mod it reports is
 * either in [ALWAYS_ALLOWED_MODS] or in [allowedMods].
 *
 * @property allowedMods the ids of the mods allowed in addition to [ALWAYS_ALLOWED_MODS]
 */
class HandshakeEvaluator(private val allowedMods: Set<String>) {

    /**
     * Evaluates a client's hello.
     *
     * @param hello the hello the client sent, or `null` if none arrived in time
     * @return [HandshakeOutcome.MissingMod] without a hello, [HandshakeOutcome.ProtocolMismatch]
     *         if the protocol versions differ, [HandshakeOutcome.ForbiddenMods] if any reported
     *         mod is not allowed, and [HandshakeOutcome.Accepted] otherwise
     */
    fun evaluate(hello: ClientHello?): HandshakeOutcome {
        if (hello == null) return HandshakeOutcome.MissingMod
        if (hello.protocolVersion != PROTOCOL_VERSION) {
            return HandshakeOutcome.ProtocolMismatch(hello.protocolVersion, PROTOCOL_VERSION)
        }

        val forbidden = hello.loadedMods.asSequence()
            .map { it.id }
            .filter { it !in ALWAYS_ALLOWED_MODS && it !in allowedMods }
            .distinct()
            .sorted()
            .toList()
        return if (forbidden.isEmpty()) HandshakeOutcome.Accepted else HandshakeOutcome.ForbiddenMods(forbidden)
    }

    /**
     * Holds the mods every client may load.
     */
    companion object {
        /**
         * The ids of the mods every client may load: the game, the Java runtime, the Fabric
         * loader with its bundled MixinExtras, Fabric API, Fabric Language Kotlin and the roleplay
         * mod itself.
         */
        val ALWAYS_ALLOWED_MODS: Set<String> = setOf(
            "minecraft",
            "java",
            "fabricloader",
            "mixinextras",
            "fabric-api",
            "fabric-language-kotlin",
            "surf-roleplay",
        )
    }
}

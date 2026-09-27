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
}

/**
 * Decides whether a client may join, based on the hello it sent in the configuration phase.
 */
class HandshakeEvaluator {

    /**
     * Evaluates a client's hello.
     *
     * @param hello the hello the client sent, or `null` if none arrived in time
     * @return [HandshakeOutcome.MissingMod] without a hello, [HandshakeOutcome.ProtocolMismatch]
     *         if the protocol versions differ, and [HandshakeOutcome.Accepted] otherwise
     */
    fun evaluate(hello: ClientHello?): HandshakeOutcome = when {
        hello == null -> HandshakeOutcome.MissingMod
        hello.protocolVersion != PROTOCOL_VERSION ->
            HandshakeOutcome.ProtocolMismatch(hello.protocolVersion, PROTOCOL_VERSION)

        else -> HandshakeOutcome.Accepted
    }
}

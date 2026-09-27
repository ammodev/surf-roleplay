package dev.slne.surf.roleplay.protocol.packets

import dev.slne.surf.roleplay.protocol.Packet
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The server's answer to a [ClientHello].
 *
 * A rejected client is disconnected right after this packet.
 *
 * @property accepted whether the client may continue into the world
 * @property reason why the client was rejected, or `null` if it was accepted
 */
@Serializable
data class HandshakeResult(
    @ProtoNumber(1) val accepted: Boolean,
    @ProtoNumber(2) val reason: String? = null,
) : Packet

package dev.slne.surf.roleplay.protocol.packets

import dev.slne.surf.roleplay.protocol.Packet
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The first packet of the handshake, sent by the client mod in the configuration phase.
 *
 * @property protocolVersion the protocol version the client mod was built with
 * @property modVersion the version of the client mod
 * @property loadedMods every mod the client has loaded, including the roleplay mod itself
 */
@Serializable
data class ClientHello(
    @ProtoNumber(1) val protocolVersion: Int,
    @ProtoNumber(2) val modVersion: String,
    @ProtoNumber(3) val loadedMods: List<ModInfo> = emptyList(),
) : Packet

/**
 * A mod loaded by the client.
 *
 * @property id the mod id
 * @property version the mod version
 */
@Serializable
data class ModInfo(
    @ProtoNumber(1) val id: String,
    @ProtoNumber(2) val version: String,
)

package dev.slne.surf.roleplay.protocol

/**
 * The version of the packet protocol spoken by this build of the protocol module.
 *
 * A client and a server can talk to each other only if both report the same version.
 */
const val PROTOCOL_VERSION: Int = 1

/**
 * The namespace of every payload channel of the roleplay protocol.
 */
const val PROTOCOL_NAMESPACE: String = "roleplay"

package dev.slne.surf.roleplay.protocol

import dev.slne.surf.roleplay.protocol.packets.ClientHello
import dev.slne.surf.roleplay.protocol.packets.Welcome
import dev.slne.surf.roleplay.protocol.screen.ScreenClose
import dev.slne.surf.roleplay.protocol.screen.ScreenClosed
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenPatch
import dev.slne.surf.roleplay.protocol.screen.ScreenTypedAction
import dev.slne.surf.roleplay.protocol.screen.ScreenTypedUpdate
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction

/**
 * The registry of every [PacketType] of the roleplay protocol.
 *
 * Both the server and the client mod register exactly the packet types in [all].
 */
object Packets {
    /**
     * The hello the client mod sends in the configuration phase to start the handshake.
     */
    val HELLO: PacketType<ClientHello> = PacketType(
        "hello",
        PacketDirection.SERVERBOUND,
        setOf(ConnectionPhase.CONFIGURATION),
        ClientHello.serializer(),
    )

    /**
     * The welcome the server sends once the player is in the world, which marks the server as the
     * roleplay server for the client mod.
     */
    val WELCOME: PacketType<Welcome> = PacketType(
        "welcome",
        PacketDirection.CLIENTBOUND,
        setOf(ConnectionPhase.PLAY),
        Welcome.serializer(),
    )

    /**
     * Opens a screen for the player.
     */
    val SCREEN_OPEN: PacketType<ScreenOpen> = PacketType(
        "screen_open",
        PacketDirection.CLIENTBOUND,
        setOf(ConnectionPhase.PLAY),
        ScreenOpen.serializer(),
    )

    /**
     * Changes the tree of an open generic screen.
     */
    val SCREEN_PATCH: PacketType<ScreenPatch> = PacketType(
        "screen_patch",
        PacketDirection.CLIENTBOUND,
        setOf(ConnectionPhase.PLAY),
        ScreenPatch.serializer(),
    )

    /**
     * Replaces the state of an open typed screen.
     */
    val SCREEN_TYPED_UPDATE: PacketType<ScreenTypedUpdate> = PacketType(
        "screen_typed_update",
        PacketDirection.CLIENTBOUND,
        setOf(ConnectionPhase.PLAY),
        ScreenTypedUpdate.serializer(),
    )

    /**
     * Closes one open screen with the screens above it, or every open screen.
     */
    val SCREEN_CLOSE: PacketType<ScreenClose> = PacketType(
        "screen_close",
        PacketDirection.CLIENTBOUND,
        setOf(ConnectionPhase.PLAY),
        ScreenClose.serializer(),
    )

    /**
     * Reports a click on a widget of a generic screen.
     */
    val SCREEN_WIDGET_ACTION: PacketType<ScreenWidgetAction> = PacketType(
        "screen_widget_action",
        PacketDirection.SERVERBOUND,
        setOf(ConnectionPhase.PLAY),
        ScreenWidgetAction.serializer(),
    )

    /**
     * Reports an action of a typed screen.
     */
    val SCREEN_TYPED_ACTION: PacketType<ScreenTypedAction> = PacketType(
        "screen_typed_action",
        PacketDirection.SERVERBOUND,
        setOf(ConnectionPhase.PLAY),
        ScreenTypedAction.serializer(),
    )

    /**
     * Reports that the player closed a screen.
     */
    val SCREEN_CLOSED: PacketType<ScreenClosed> = PacketType(
        "screen_closed",
        PacketDirection.SERVERBOUND,
        setOf(ConnectionPhase.PLAY),
        ScreenClosed.serializer(),
    )

    /**
     * Every packet type of the protocol.
     */
    val all: List<PacketType<*>> = listOf(
        HELLO,
        WELCOME,
        SCREEN_OPEN,
        SCREEN_PATCH,
        SCREEN_TYPED_UPDATE,
        SCREEN_CLOSE,
        SCREEN_WIDGET_ACTION,
        SCREEN_TYPED_ACTION,
        SCREEN_CLOSED,
    )

    /**
     * The packet types keyed by their channel id.
     */
    private val typesByChannel: Map<String, PacketType<*>> = all.associateBy { it.channel }

    init {
        check(typesByChannel.size == all.size) { "Two packet types share a channel" }
    }

    /**
     * Looks up the packet type that travels on a channel.
     *
     * @param channel the payload channel id, such as `roleplay:hello`
     * @return the packet type, or `null` if no packet type uses the channel
     */
    fun byChannel(channel: String): PacketType<*>? = typesByChannel[channel]
}

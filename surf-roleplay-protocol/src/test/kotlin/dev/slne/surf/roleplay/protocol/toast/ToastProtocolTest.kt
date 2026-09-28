package dev.slne.surf.roleplay.protocol.toast

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the toast packets on the wire.
 */
class ToastProtocolTest {

    /**
     * Verifies that a toast with every field survives a round trip.
     */
    @Test
    fun `toast show round-trips`() {
        val toast = ToastShow(
            id = "fine-1",
            type = ToastType.WARNING,
            title = "\"Strafe\"",
            description = "\"50 € wurden abgebucht.\"",
            actionLabel = "\"Anfechten\"",
            cancelLabel = "\"Ok\"",
            durationMillis = 6000,
            dismissible = false,
            closeButton = true,
        )

        assertEquals(toast, ProtocolCodec.decode(Packets.TOAST_SHOW.channel, ProtocolCodec.encode(Packets.TOAST_SHOW, toast)))
    }

    /**
     * Verifies that the dismissal and the action survive a round trip.
     */
    @Test
    fun `dismiss and action round-trip`() {
        val dismiss = ToastDismiss("fine-1")
        val action = ToastAction("fine-1", ToastButtonKind.CANCEL)

        assertEquals(dismiss, ProtocolCodec.decode(Packets.TOAST_DISMISS.channel, ProtocolCodec.encode(Packets.TOAST_DISMISS, dismiss)))
        assertEquals(action, ProtocolCodec.decode(Packets.TOAST_ACTION.channel, ProtocolCodec.encode(Packets.TOAST_ACTION, action)))
    }
}

package dev.slne.surf.roleplay.paper.toast

import dev.slne.surf.roleplay.api.client.common.toast.Toast
import dev.slne.surf.roleplay.api.client.common.toast.ToastButton
import dev.slne.surf.roleplay.api.client.common.toast.ToastType
import dev.slne.surf.roleplay.protocol.toast.ToastButtonKind
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

/**
 * Tests for the per-player registry of active toasts.
 */
class ToastRegistryTest {

    /**
     * The clicks the toast buttons reported.
     */
    private val clicks = mutableListOf<String>()

    /**
     * A toast with an action and a cancel button that record their clicks.
     */
    private val toast = Toast(
        Component.text("Anruf"),
        action = ToastButton(Component.text("Annehmen")) { clicks += "accept:${it.toastId}" },
        cancel = ToastButton(Component.text("Ablehnen")) { clicks += "decline:${it.toastId}" },
        durationMillis = 5000,
    )

    /**
     * Verifies that a click on a button of an active toast runs its handler once.
     */
    @Test
    fun `buttons of active toasts run once`() {
        val registry = ToastRegistry()
        val id = registry.register(toast, now = 0)

        val first = registry.handle(id, ToastButtonKind.ACTION, now = 1000)
        assertIs<ToastRegistry.Outcome.Accepted>(first).run()
        assertIs<ToastRegistry.Outcome.Rejected>(registry.handle(id, ToastButtonKind.CANCEL, now = 1100))

        assertEquals(listOf("accept:$id"), clicks)
    }

    /**
     * Verifies that unknown toasts, missing buttons and expired toasts are rejected.
     */
    @Test
    fun `unknown, missing and expired buttons are rejected`() {
        val registry = ToastRegistry()
        val plain = registry.register(Toast(Component.text("Hinweis")), now = 0)
        val timed = registry.register(toast, now = 0)

        assertIs<ToastRegistry.Outcome.Rejected>(registry.handle("nope", ToastButtonKind.ACTION, now = 10))
        assertIs<ToastRegistry.Outcome.Rejected>(registry.handle(plain, ToastButtonKind.ACTION, now = 10))
        assertIs<ToastRegistry.Outcome.Rejected>(registry.handle(timed, ToastButtonKind.ACTION, now = 5000 + ToastRegistry.GRACE_MILLIS + 1))
    }

    /**
     * Verifies that a dismissed toast no longer accepts clicks, and that showing a toast with the
     * id of an active one replaces it.
     */
    @Test
    fun `dismissed toasts are gone and ids are replaced`() {
        val registry = ToastRegistry()
        val id = registry.register(toast.copy(id = "call"), now = 0)
        assertEquals("call", id)

        registry.register(Toast(Component.text("Verpasst"), id = "call"), now = 10)
        assertIs<ToastRegistry.Outcome.Rejected>(registry.handle("call", ToastButtonKind.ACTION, now = 20))

        val other = registry.register(toast, now = 30)
        registry.dismiss(other)
        assertIs<ToastRegistry.Outcome.Rejected>(registry.handle(other, ToastButtonKind.ACTION, now = 40))
    }

    /**
     * Verifies that toasts without an id get distinct ids, that loading toasts stay until they are
     * dismissed, and that the registry keeps at most its limit of toasts.
     */
    @Test
    fun `ids, loading and the limit`() {
        val registry = ToastRegistry()
        val a = registry.register(Toast(Component.text("A")), now = 0)
        val b = registry.register(Toast(Component.text("B")), now = 0)
        assertNotEquals(a, b)

        val loading = registry.register(toast.copy(type = ToastType.LOADING, durationMillis = 0), now = 0)
        val accepted = registry.handle(loading, ToastButtonKind.ACTION, now = ToastRegistry.MAX_AGE_MILLIS - 1)
        assertIs<ToastRegistry.Outcome.Accepted>(accepted)

        val first = registry.register(toast, now = 0)
        repeat(ToastRegistry.MAX_ACTIVE) { registry.register(toast, now = 0) }
        assertIs<ToastRegistry.Outcome.Rejected>(registry.handle(first, ToastButtonKind.ACTION, now = 10))
    }
}

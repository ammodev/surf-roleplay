package dev.slne.surf.roleplay.core.common.license

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.roleplay.api.common.license.licenses.CarLicense
import dev.slne.surf.roleplay.api.common.license.licenses.TruckLicense
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests for [CoreLicenseRegistry].
 */
class CoreLicenseRegistryTest {

    @Test
    fun `registers the car and truck license definitions on construction`() {
        val registry = CoreLicenseRegistry()

        assertEquals(setOf(CarLicense, TruckLicense), registry.licenses.toSet())
    }

    @Test
    fun `getByKey resolves a registered license by its key`() {
        val registry = CoreLicenseRegistry()

        assertSame(CarLicense, registry.getByKey(CarLicense.key))
        assertSame(TruckLicense, registry.getByKey(TruckLicense.key))
    }

    @Test
    fun `getByKey returns null for an unregistered key`() {
        val registry = CoreLicenseRegistry()

        assertNull(registry.getByKey(key("roleplay", "unknown_license")))
    }

    @Test
    fun `unregister removes a license so it can no longer be resolved`() {
        val registry = CoreLicenseRegistry()

        registry.unregister(CarLicense)

        assertNull(registry.getByKey(CarLicense.key))
    }
}

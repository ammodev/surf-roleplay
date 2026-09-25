package dev.slne.surf.roleplay.core.client.common.license

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.util.objectListOf
import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.licenses.CarLicense
import dev.slne.surf.roleplay.api.common.license.licenses.TruckLicense
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for [CoreLicenseRegistry].
 */
class CoreLicenseRegistryTest {

    /**
     * Verifies that constructing the registry registers the car and truck license definitions.
     */
    @Test
    fun `registers the car and truck license definitions on construction`() {
        val registry = CoreLicenseRegistry()

        assertEquals(setOf(CarLicense, TruckLicense), registry.licenses.toSet())
    }

    /**
     * Verifies that getByKey resolves a registered license by its key.
     */
    @Test
    fun `getByKey resolves a registered license by its key`() {
        val registry = CoreLicenseRegistry()

        assertSame(CarLicense, registry.getByKey(CarLicense.key))
        assertSame(TruckLicense, registry.getByKey(TruckLicense.key))
    }

    /**
     * Verifies that getByKey returns null for an unregistered key.
     */
    @Test
    fun `getByKey returns null for an unregistered key`() {
        val registry = CoreLicenseRegistry()

        assertNull(registry.getByKey(key("roleplay", "unknown_license")))
    }

    /**
     * Verifies that unregister removes a license so it can no longer be resolved.
     */
    @Test
    fun `unregister removes a license so it can no longer be resolved`() {
        val registry = CoreLicenseRegistry()

        registry.unregister(CarLicense)

        assertNull(registry.getByKey(CarLicense.key))
    }

    /**
     * Verifies that register throws when a license is already registered under that key.
     */
    @Test
    fun `register throws when a license is already registered under that key`() {
        val registry = CoreLicenseRegistry()
        val duplicate = object : License(
            key = CarLicense.key,
            displayName = { text("Doppelter Führerschein") },
            requirements = objectListOf()
        ) {}

        assertFailsWith<IllegalArgumentException> {
            registry.register(duplicate)
        }
    }

    /**
     * Verifies that unregister does nothing when a different instance is registered under that key.
     */
    @Test
    fun `unregister does nothing when a different instance is registered under that key`() {
        val registry = CoreLicenseRegistry()
        val other = object : License(
            key = key("roleplay", "unknown_license"),
            displayName = { text("Andere Lizenz") },
            requirements = objectListOf()
        ) {}

        registry.unregister(other)

        assertSame(CarLicense, registry.getByKey(CarLicense.key))
    }

    /**
     * Verifies that the licenses view reflects registrations made after it was obtained.
     */
    @Test
    fun `licenses is a live view that reflects later registrations`() {
        val registry = CoreLicenseRegistry()
        val view = registry.licenses
        val additional = object : License(
            key = key("roleplay", "additional_license"),
            displayName = { text("Weitere Lizenz") },
            requirements = objectListOf()
        ) {}

        assertFalse(view.contains(additional))

        registry.register(additional)

        assertTrue(view.contains(additional))
    }
}

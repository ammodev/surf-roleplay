package dev.slne.surf.roleplay.api.common.license

import dev.slne.surf.api.core.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.UnmodifiableView

private val registry = requiredService<LicenseRegistry>()

/**
 * Holds every known [License] definition and resolves them by their [Key].
 */
interface LicenseRegistry {
    /** All license definitions currently known to the registry. */
    val licenses: @UnmodifiableView ObjectSet<License>

    /** Adds a license definition to the registry. */
    fun register(license: License)

    /** Removes a license definition from the registry. */
    fun unregister(license: License)

    /** Resolves the license definition registered under [key], or `null` if none is registered. */
    fun getByKey(key: Key): License?

    /** Provides access to the single registered [LicenseRegistry] service. */
    companion object : LicenseRegistry by registry {
        /** The single registered [LicenseRegistry] service instance. */
        val INSTANCE get() = registry
    }
}
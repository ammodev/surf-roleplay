package dev.slne.surf.roleplay.core.common.license

import com.google.auto.service.AutoService
import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.LicenseRegistry
import dev.slne.surf.roleplay.api.common.license.licenses.CarLicense
import dev.slne.surf.roleplay.api.common.license.licenses.TruckLicense
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSets
import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentHashMap

/**
 * Default [LicenseRegistry] implementation, pre-populated with every license definition known
 * to the roleplay plugin.
 *
 * License definitions are stored keyed by their [License.key] in a [ConcurrentHashMap], so
 * [register], [unregister] and [getByKey] are all safe to call concurrently from multiple
 * threads.
 */
@AutoService(LicenseRegistry::class)
class CoreLicenseRegistry : LicenseRegistry {
    private val licensesByKey = ConcurrentHashMap<Key, License>()

    /** A snapshot of every license definition currently registered. */
    override val licenses: ObjectSet<License>
        get() = ObjectSets.unmodifiable(ObjectOpenHashSet(licensesByKey.values))

    init {
        register(CarLicense)
        register(TruckLicense)
    }

    /**
     * Adds [license] to the registry.
     *
     * @throws IllegalArgumentException if a license is already registered under [License.key]
     */
    override fun register(license: License) {
        val existing = licensesByKey.putIfAbsent(license.key, license)
        require(existing == null) { "A license is already registered under key '${license.key}'" }
    }

    /**
     * Removes [license] from the registry, if it is still the instance registered under its
     * [License.key]. Does nothing otherwise.
     */
    override fun unregister(license: License) {
        licensesByKey.remove(license.key, license)
    }

    override fun getByKey(key: Key): License? = licensesByKey[key]
}

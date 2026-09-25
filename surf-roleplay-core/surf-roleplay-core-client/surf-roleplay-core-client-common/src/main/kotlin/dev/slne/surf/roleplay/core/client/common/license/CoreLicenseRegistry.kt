package dev.slne.surf.roleplay.core.client.common.license

import com.google.auto.service.AutoService
import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.LicenseRegistry
import dev.slne.surf.roleplay.api.common.license.licenses.CarLicense
import dev.slne.surf.roleplay.api.common.license.licenses.TruckLicense
import it.unimi.dsi.fastutil.objects.AbstractObjectSet
import it.unimi.dsi.fastutil.objects.ObjectIterator
import it.unimi.dsi.fastutil.objects.ObjectIterators
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

    /**
     * A live, unmodifiable view over every license definition currently registered.
     *
     * The view reflects later [register] and [unregister] calls; iterating it while the
     * registry is concurrently modified is weakly consistent, as for [ConcurrentHashMap.values].
     */
    override val licenses: ObjectSet<License> = ObjectSets.unmodifiable(LicenseSetView())

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

    /**
     * Resolves the license registered under [key], or `null` if none is registered.
     */
    override fun getByKey(key: Key): License? = licensesByKey[key]

    /**
     * A live [ObjectSet] view over [licensesByKey]'s values.
     *
     * Membership is checked by confirming the candidate is still the instance registered under
     * its own [License.key], not merely present anywhere in the backing map's values.
     */
    private inner class LicenseSetView : AbstractObjectSet<License>() {
        /**
         * Iterates the licenses currently registered in [licensesByKey].
         */
        override fun iterator(): ObjectIterator<License> =
            ObjectIterators.asObjectIterator(licensesByKey.values.iterator())

        /**
         * The number of licenses currently registered in [licensesByKey].
         */
        override val size: Int get() = licensesByKey.size

        /**
         * Checks whether [element] is the instance currently registered under its own key.
         */
        override fun contains(element: License): Boolean = licensesByKey[element.key] === element
    }
}

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

/**
 * Default [LicenseRegistry] implementation, pre-populated with every license definition known
 * to the roleplay plugin.
 */
@AutoService(LicenseRegistry::class)
class CoreLicenseRegistry : LicenseRegistry {
    private val backingLicenses = ObjectOpenHashSet<License>()

    override val licenses: ObjectSet<License> = ObjectSets.unmodifiable(backingLicenses)

    init {
        register(CarLicense)
        register(TruckLicense)
    }

    override fun register(license: License) {
        backingLicenses.add(license)
    }

    override fun unregister(license: License) {
        backingLicenses.remove(license)
    }

    override fun getByKey(key: Key): License? = backingLicenses.find { it.key == key }
}

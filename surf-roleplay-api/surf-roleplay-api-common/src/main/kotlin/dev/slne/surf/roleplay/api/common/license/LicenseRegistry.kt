package dev.slne.surf.roleplay.api.common.license

import dev.slne.surf.api.core.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

private val registry = requiredService<LicenseRegistry>()

interface LicenseRegistry {
    val licenses: @UnmodifiableView ObjectSet<License>

    fun register(license: License)
    fun unregister(license: License)

    fun getByUuid(uuid: UUID): License?
    fun getByName(name: String): License?

    companion object : LicenseRegistry by registry {
        val INSTANCE get() = registry
    }
}
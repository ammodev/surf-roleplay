package dev.slne.surf.roleplay.api.common.identity.identities.sar

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.roleplay.api.common.identity.qualification.IdentityQualification
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * A qualification a search-and-rescue identity can hold in addition to its rank.
 */
sealed class SarQualification(
    key: Key,
    displayName: Component
) : IdentityQualification(key, displayName) {

    /**
     * Qualification to perform technical rescue operations.
     */
    data object Rescue : SarQualification(
        key("roleplay", "sar_qualification_rescue"),
        Component.text("Technische Rettung")
    )

    /**
     * Qualification to perform water rescue operations.
     */
    data object WaterRescue : SarQualification(
        key("roleplay", "sar_qualification_water_rescue"),
        Component.text("Wasserrettung")
    )

    /**
     * Qualification to perform mountain rescue operations.
     */
    data object MountainRescue : SarQualification(
        key("roleplay", "sar_qualification_mountain_rescue"),
        Component.text("Bergrettung")
    )

    /**
     * Qualification to perform air rescue operations.
     */
    data object AirRescue : SarQualification(
        key("roleplay", "sar_qualification_air_rescue"),
        Component.text("Luftrettung")
    )

    /**
     * Lists all search-and-rescue qualifications and resolves them by key.
     */
    companion object {
        /**
         * All search-and-rescue qualifications.
         */
        val entries: List<SarQualification> by lazy {
            listOf(
                Rescue,
                WaterRescue,
                MountainRescue,
                AirRescue,
            )
        }

        /**
         * All search-and-rescue qualifications, by key.
         */
        private val byKey: Map<Key, SarQualification> by lazy {
            entries.associateBy { it.key }
        }

        /**
         * Resolves the search-and-rescue qualification stored under [key], or `null` if none matches.
         */
        fun byKey(key: Key): SarQualification? = byKey[key]
    }
}

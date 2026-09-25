package dev.slne.surf.roleplay.api.common.identity.identities.police

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.roleplay.api.common.identity.qualification.IdentityQualification
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * A qualification a police identity can hold in addition to its rank.
 */
sealed class PoliceQualification(
    key: Key,
    displayName: Component
) : IdentityQualification(key, displayName) {

    /**
     * Membership in the special forces unit (SEK).
     */
    data object SpecialForces : PoliceQualification(
        key("roleplay", "police_qualification_special_forces"),
        Component.text("Spezialeinsatzkommando (SEK)")
    )

    /**
     * Qualification to work as a canine handler.
     */
    data object DogHandler : PoliceQualification(
        key("roleplay", "police_qualification_dog_handler"),
        Component.text("Diensthundeführer")
    )

    /**
     * Qualification to work in criminal investigation.
     */
    data object CriminalInvestigation : PoliceQualification(
        key("roleplay", "police_qualification_criminal_investigation"),
        Component.text("Kriminalpolizei")
    )

    /**
     * Qualification to work in traffic policing.
     */
    data object TrafficPolice : PoliceQualification(
        key("roleplay", "police_qualification_traffic_police"),
        Component.text("Verkehrspolizei")
    )

    /**
     * Qualification to pilot a police helicopter.
     */
    data object HelicopterPilot : PoliceQualification(
        key("roleplay", "police_qualification_helicopter_pilot"),
        Component.text("Hubschrauberpilot")
    )

    /**
     * Lists all police qualifications and resolves them by key.
     */
    companion object {
        /**
         * All police qualifications.
         */
        val entries: List<PoliceQualification> by lazy {
            listOf(
                SpecialForces,
                DogHandler,
                CriminalInvestigation,
                TrafficPolice,
                HelicopterPilot,
            )
        }

        /**
         * All police qualifications, by key.
         */
        private val byKey: Map<Key, PoliceQualification> by lazy {
            entries.associateBy { it.key }
        }

        /**
         * Resolves the police qualification stored under [key], or `null` if none matches.
         */
        fun byKey(key: Key): PoliceQualification? = byKey[key]
    }
}

package dev.slne.surf.roleplay.api.common.identity.identities.sar

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.roleplay.api.common.identity.rank.IdentityRank
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * A rank within the search-and-rescue service, in ascending order of seniority.
 */
sealed class SarRank(
    key: Key,
    displayName: Component,
    level: Int
) : IdentityRank(key, displayName, level) {

    /**
     * The entry-level search-and-rescue rank.
     */
    data object RescueAssistant : SarRank(
        key("roleplay", "sar_rank_rescue_assistant"),
        Component.text("Rettungshelfer"),
        1
    )

    /**
     * The rank of a trained emergency medical technician.
     */
    data object EmergencyMedicalTechnician : SarRank(
        key("roleplay", "sar_rank_emergency_medical_technician"),
        Component.text("Rettungssanitäter"),
        2
    )

    /**
     * The rank of a paramedic.
     */
    data object Paramedic : SarRank(
        key("roleplay", "sar_rank_paramedic"),
        Component.text("Notfallsanitäter"),
        3
    )

    /**
     * The rank of an emergency physician.
     */
    data object EmergencyPhysician : SarRank(
        key("roleplay", "sar_rank_emergency_physician"),
        Component.text("Notarzt"),
        4
    )

    /**
     * The rank of a lead emergency physician.
     */
    data object LeadEmergencyPhysician : SarRank(
        key("roleplay", "sar_rank_lead_emergency_physician"),
        Component.text("Leitender Notarzt"),
        5
    )

    /**
     * The highest rank within the search-and-rescue service.
     */
    data object MedicalDirector : SarRank(
        key("roleplay", "sar_rank_medical_director"),
        Component.text("Ärztlicher Leiter Rettungsdienst"),
        6
    )

    companion object {
        /**
         * All search-and-rescue ranks, in ascending order of seniority.
         */
        val entries: List<SarRank> by lazy {
            listOf(
                RescueAssistant,
                EmergencyMedicalTechnician,
                Paramedic,
                EmergencyPhysician,
                LeadEmergencyPhysician,
                MedicalDirector,
            )
        }

        private val byKey: Map<Key, SarRank> by lazy {
            entries.associateBy { it.key }
        }

        /**
         * Resolves the search-and-rescue rank stored under [key], or `null` if none matches.
         */
        fun byKey(key: Key): SarRank? = byKey[key]
    }
}

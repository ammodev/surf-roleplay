package dev.slne.surf.roleplay.api.common.identity.identities.police

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.roleplay.api.common.identity.rank.IdentityRank
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * A rank within the police force, in ascending order of seniority.
 */
sealed class PoliceRank(
    key: Key,
    displayName: Component,
    level: Int
) : IdentityRank(key, displayName, level) {

    /**
     * The entry-level police rank.
     */
    data object Cadet : PoliceRank(
        key("roleplay", "police_rank_cadet"),
        Component.text("Polizeimeisteranwärter"),
        1
    )

    /**
     * The rank held after completing cadet training.
     */
    data object PoliceOfficer : PoliceRank(
        key("roleplay", "police_rank_police_officer"),
        Component.text("Polizeimeister"),
        2
    )

    /**
     * A senior police officer rank.
     */
    data object SeniorPoliceOfficer : PoliceRank(
        key("roleplay", "police_rank_senior_police_officer"),
        Component.text("Polizeiobermeister"),
        3
    )

    /**
     * A master police officer rank.
     */
    data object MasterPoliceOfficer : PoliceRank(
        key("roleplay", "police_rank_master_police_officer"),
        Component.text("Polizeihauptmeister"),
        4
    )

    /**
     * The entry-level rank for the higher police career track.
     */
    data object Inspector : PoliceRank(
        key("roleplay", "police_rank_inspector"),
        Component.text("Polizeikommissar"),
        5
    )

    /**
     * A senior inspector rank.
     */
    data object SeniorInspector : PoliceRank(
        key("roleplay", "police_rank_senior_inspector"),
        Component.text("Polizeioberkommissar"),
        6
    )

    /**
     * A chief inspector rank.
     */
    data object ChiefInspector : PoliceRank(
        key("roleplay", "police_rank_chief_inspector"),
        Component.text("Polizeihauptkommissar"),
        7
    )

    /**
     * The senior-most chief inspector rank.
     */
    data object FirstChiefInspector : PoliceRank(
        key("roleplay", "police_rank_first_chief_inspector"),
        Component.text("Erster Polizeihauptkommissar"),
        8
    )

    /**
     * A superintendent rank.
     */
    data object Superintendent : PoliceRank(
        key("roleplay", "police_rank_superintendent"),
        Component.text("Polizeirat"),
        9
    )

    /**
     * A senior superintendent rank.
     */
    data object SeniorSuperintendent : PoliceRank(
        key("roleplay", "police_rank_senior_superintendent"),
        Component.text("Polizeioberrat"),
        10
    )

    /**
     * The rank of a police director.
     */
    data object PoliceDirector : PoliceRank(
        key("roleplay", "police_rank_police_director"),
        Component.text("Polizeidirektor"),
        11
    )

    /**
     * The rank of a leading police director.
     */
    data object LeadingPoliceDirector : PoliceRank(
        key("roleplay", "police_rank_leading_police_director"),
        Component.text("Leitender Polizeidirektor"),
        12
    )

    /**
     * The highest rank within the police force.
     */
    data object PolicePresident : PoliceRank(
        key("roleplay", "police_rank_police_president"),
        Component.text("Polizeipräsident"),
        13
    )

    /**
     * Lists all police ranks and resolves them by key.
     */
    companion object {
        /**
         * All police ranks, in ascending order of seniority.
         */
        val entries: List<PoliceRank> by lazy {
            listOf(
                Cadet,
                PoliceOfficer,
                SeniorPoliceOfficer,
                MasterPoliceOfficer,
                Inspector,
                SeniorInspector,
                ChiefInspector,
                FirstChiefInspector,
                Superintendent,
                SeniorSuperintendent,
                PoliceDirector,
                LeadingPoliceDirector,
                PolicePresident,
            )
        }

        /**
         * All police ranks, by key.
         */
        private val byKey: Map<Key, PoliceRank> by lazy {
            entries.associateBy { it.key }
        }

        /**
         * Resolves the police rank stored under [key], or `null` if none matches.
         */
        fun byKey(key: Key): PoliceRank? = byKey[key]
    }
}

package dev.slne.surf.roleplay.paper.tablist

import dev.slne.surf.roleplay.api.client.paper.tablist.OrganisationCountProvider
import dev.slne.surf.roleplay.api.client.paper.tablist.SelfInfoProvider
import dev.slne.surf.roleplay.api.client.paper.tablist.TabListService
import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceIdentity
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarIdentity
import dev.slne.surf.roleplay.api.common.user.RoleplayUser
import dev.slne.surf.roleplay.core.common.user.UserStateListeners
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import java.util.UUID

/**
 * The built-in tab list providers based on the active identity of each player: one organisation
 * per identity type, counting the players whose active identity has that type, and the rank of
 * the active police or SAR identity. The character name is not known and is left empty.
 *
 * Every provider reads only users held in memory.
 *
 * @property users returns the user held in memory for a player UUID, or `null` if none is held
 */
class IdentityProviders(private val users: (UUID) -> RoleplayUser?) {

    /**
     * An organisation whose members are the players with an active identity of one type.
     *
     * @property key the stable identifier of the organisation
     * @property label the German display name
     * @property icon the Lucide icon name
     * @property type the identity type of the members
     */
    inner class IdentityOrganisation(
        override val key: String,
        override val label: Component,
        override val icon: String,
        private val type: IdentityType,
    ) : OrganisationCountProvider {

        /**
         * Tells whether a player's active identity has this organisation's type.
         *
         * @param player the player
         * @return `true` if the player is counted
         */
        override fun counts(player: Player): Boolean = isMember(player.uniqueId)

        /**
         * Tells whether the held user of a player has an active identity of this organisation's
         * type.
         *
         * @param player the player's UUID
         * @return `true` if the player is counted
         */
        fun isMember(player: UUID): Boolean = users(player)?.activeIdentity?.type == type
    }

    /**
     * Provides the rank of a player's active police or SAR identity.
     */
    inner class IdentitySelfInfo : SelfInfoProvider {

        /**
         * Returns the German name of the rank of the player's active identity.
         *
         * @param player the player
         * @return the rank, or `null` if the player has no active police or SAR identity
         */
        override fun rank(player: Player): String? = rankOf(player.uniqueId)

        /**
         * Returns the German name of the rank of the active identity of a player's held user.
         *
         * @param player the player's UUID
         * @return the rank, or `null` if the player has no active police or SAR identity or no
         *         held user
         */
        fun rankOf(player: UUID): String? = when (val identity = users(player)?.activeIdentity) {
            is PoliceIdentity -> plain(identity.rank.displayName)
            is SarIdentity -> plain(identity.rank.displayName)
            else -> null
        }
    }

    /**
     * The built-in organisations in display order: civilians, police and SAR.
     */
    val organisations: List<IdentityOrganisation> = listOf(
        IdentityOrganisation("civilian", Component.text("Zivilisten"), "users", IdentityType.CIVILIAN),
        IdentityOrganisation("police", Component.text("Polizei"), "shield", IdentityType.POLICE),
        IdentityOrganisation("sar", Component.text("SAR"), "ambulance", IdentityType.SAR),
    )

    /**
     * The built-in self-info provider.
     */
    val selfInfo: IdentitySelfInfo = IdentitySelfInfo()

    /**
     * Registers the built-in providers with a tab list service, and a user state listener that
     * marks every player's tab list as changed whenever a user's identity state changes, since
     * that may change every player's organisation counts.
     *
     * @param service the tab list service
     * @return a handle that unregisters the user state listener when closed
     */
    fun register(service: TabListService): AutoCloseable {
        organisations.forEach(service::registerOrganisation)
        service.registerSelfInfo(selfInfo)
        return UserStateListeners.register { service.changed() }
    }

    /**
     * Holds the plain-text conversion of display names.
     */
    companion object {
        /**
         * Returns the plain text of a component.
         *
         * @param component the component
         * @return its text without formatting
         */
        fun plain(component: Component): String = PlainTextComponentSerializer.plainText().serialize(component)
    }
}

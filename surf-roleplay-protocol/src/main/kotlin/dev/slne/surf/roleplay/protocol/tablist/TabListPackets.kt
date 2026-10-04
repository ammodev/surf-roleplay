package dev.slne.surf.roleplay.protocol.tablist

import dev.slne.surf.roleplay.protocol.Packet
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * A coarse count of how many members of an organisation are online.
 */
@Serializable
enum class OnlineLevel {
    /**
     * Nobody is online.
     */
    @ProtoNumber(0)
    NONE,

    /**
     * A few members are online.
     */
    @ProtoNumber(1)
    FEW,

    /**
     * Many members are online.
     */
    @ProtoNumber(2)
    MANY,
}

/**
 * The online count of one organisation shown in the tab list.
 *
 * @property key the stable identifier of the organisation
 * @property label the display name as component JSON
 * @property icon the icon name, empty for none
 * @property level the coarse online level
 * @property exact the exact online count, only set when the organisation is configured to show it
 */
@Serializable
data class OrganisationCount(
    @ProtoNumber(1) val key: String,
    @ProtoNumber(2) val label: String,
    @ProtoNumber(3) val icon: String = "",
    @ProtoNumber(4) val level: OnlineLevel = OnlineLevel.NONE,
    @ProtoNumber(5) val exact: Int? = null,
)

/**
 * The complete state shown in the tab list of one player.
 *
 * @property organisations the organisation counts in display order
 * @property onlineTotal the number of players online
 * @property serverTimeMillis the server time in epoch milliseconds when the state was built
 * @property zoneId the time zone id used to show the clock
 * @property restartAtMillis the next restart in epoch milliseconds, or `null` if none is scheduled
 * @property weather the weather description as component JSON
 * @property announcement the announcement as component JSON, or `null` if none is set
 * @property characterName the name of the player's character, or `null` if unknown
 * @property job the player's job, or `null` if none
 * @property rank the player's rank, or `null` if none
 * @property sessionStartMillis the start of the player's session in epoch milliseconds
 */
@Serializable
data class TabListState(
    @ProtoNumber(1) val organisations: List<OrganisationCount> = emptyList(),
    @ProtoNumber(2) val onlineTotal: Int = 0,
    @ProtoNumber(3) val serverTimeMillis: Long = 0,
    @ProtoNumber(4) val zoneId: String = "Europe/Berlin",
    @ProtoNumber(5) val restartAtMillis: Long? = null,
    @ProtoNumber(6) val weather: String = "",
    @ProtoNumber(7) val announcement: String? = null,
    @ProtoNumber(8) val characterName: String? = null,
    @ProtoNumber(9) val job: String? = null,
    @ProtoNumber(10) val rank: String? = null,
    @ProtoNumber(11) val sessionStartMillis: Long = 0,
) : Packet

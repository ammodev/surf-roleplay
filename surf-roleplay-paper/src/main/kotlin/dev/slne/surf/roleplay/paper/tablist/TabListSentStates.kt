package dev.slne.surf.roleplay.paper.tablist

import dev.slne.surf.roleplay.protocol.tablist.TabListState
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Remembers the last tab list state sent to each player so that a state identical to it is not
 * sent again. The server time of a state is ignored when comparing, since the client advances
 * its clock on its own. Every method is thread-safe.
 */
class TabListSentStates {

    /**
     * The last sent state of each player, with the server time cleared.
     */
    private val sent = ConcurrentHashMap<UUID, TabListState>()

    /**
     * Returns whether a state equals the one last sent to the player.
     *
     * @param player the player's unique id
     * @param state the state about to be sent
     * @return `true` if the state needs no sending
     */
    fun isUnchanged(player: UUID, state: TabListState): Boolean = sent[player] == normalise(state)

    /**
     * Records that a state was sent to a player.
     *
     * @param player the player's unique id
     * @param state the state that was sent
     */
    fun record(player: UUID, state: TabListState) {
        sent[player] = normalise(state)
    }

    /**
     * Forgets the state last sent to a player, so that the next state is sent in any case.
     *
     * @param player the player's unique id
     */
    fun forget(player: UUID) {
        sent -= player
    }

    /**
     * Returns a state with the part that changes on every build cleared.
     *
     * @param state the state
     * @return the state without its server time
     */
    private fun normalise(state: TabListState): TabListState = state.copy(serverTimeMillis = 0)
}

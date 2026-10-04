package dev.slne.surf.roleplay.fabric.tablist

import dev.slne.surf.roleplay.protocol.tablist.TabListState
import java.time.DateTimeException
import java.time.ZoneId

/**
 * Holds the last tab list state the server sent on the current connection, the monotonic time it
 * was received and its time zone. Not thread-safe; used on the client thread only.
 *
 * @param warn called with a message when the server sends a time zone that does not exist; called
 *        at most once per distinct zone id and at most [MAX_WARNED_ZONES] times in total
 */
class TabListStore(private val warn: (String) -> Unit) {

    /**
     * The last state, or `null` if none was received since the store was last cleared.
     */
    var state: TabListState? = null
        private set

    /**
     * The monotonic time in nanoseconds at which [state] was received.
     */
    var receivedAtNanos: Long = 0L
        private set

    /**
     * The time zone of [state], or [DEFAULT_ZONE] if none was received or its zone id is invalid.
     */
    var zone: ZoneId = DEFAULT_ZONE
        private set

    /**
     * The invalid zone ids already warned about.
     */
    private val warnedZones = HashSet<String>()

    /**
     * Stores a state if the roleplay state is active; a newer state replaces the older one.
     *
     * @param packet the state the server sent
     * @param active whether the roleplay state is active
     * @param nowNanos the monotonic time in nanoseconds
     * @return whether the state was stored
     */
    fun receive(packet: TabListState, active: Boolean, nowNanos: Long): Boolean {
        if (!active) return false
        state = packet
        receivedAtNanos = nowNanos
        zone = parseZone(packet.zoneId)
        return true
    }

    /**
     * Forgets the stored state and resets the zone, so that nothing of a previous server is shown.
     */
    fun clear() {
        state = null
        receivedAtNanos = 0L
        zone = DEFAULT_ZONE
    }

    /**
     * Parses a zone id, falling back to [DEFAULT_ZONE] and warning once for an invalid one.
     *
     * @param id the zone id
     * @return the zone
     */
    private fun parseZone(id: String): ZoneId = try {
        ZoneId.of(id)
    } catch (_: DateTimeException) {
        if (warnedZones.size < MAX_WARNED_ZONES && warnedZones.add(id)) warn("Unknown tab list time zone $id")
        DEFAULT_ZONE
    }

    /**
     * The default zone and the warning limit of the store.
     */
    companion object {

        /**
         * The time zone used when none was received or the received one is invalid.
         */
        val DEFAULT_ZONE: ZoneId = ZoneId.of("Europe/Berlin")

        /**
         * The most distinct invalid zone ids that are warned about.
         */
        const val MAX_WARNED_ZONES: Int = 16
    }
}

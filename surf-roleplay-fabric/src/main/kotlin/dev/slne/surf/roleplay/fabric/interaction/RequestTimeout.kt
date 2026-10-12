package dev.slne.surf.roleplay.fabric.interaction

/**
 * Decides when a request to the server counts as unanswered.
 */
object RequestTimeout {

    /**
     * How long a request waits for its answer, in milliseconds.
     */
    const val MILLIS: Long = 2000

    /**
     * Tells whether a request has waited long enough.
     *
     * @param sentAt when the request was sent, in milliseconds
     * @param now the current time, in milliseconds
     * @return whether [MILLIS] have passed since [sentAt]
     */
    fun expired(sentAt: Long, now: Long): Boolean = now - sentAt >= MILLIS
}

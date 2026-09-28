package dev.slne.surf.roleplay.fabric.screen

/**
 * The stack of a player's open server-driven screens, from the bottom screen to the shown one.
 *
 * @param T the content kept for each screen
 */
class ScreenStack<T> {

    /**
     * One open screen.
     *
     * @param T the content kept for the screen
     * @property sessionId the server-issued session id of the screen
     * @property closable whether the player can close the screen with Escape
     * @property content the content kept for the screen
     */
    data class Entry<T>(val sessionId: Int, val closable: Boolean, val content: T)

    /**
     * The open screens, from bottom to top.
     */
    private val stack = ArrayDeque<Entry<T>>()

    /**
     * The open screens, from bottom to top.
     */
    val entries: List<Entry<T>> get() = stack.toList()

    /**
     * The screen that is shown, or `null` if no screen is open.
     */
    val top: Entry<T>? get() = stack.lastOrNull()

    /**
     * Finds an open screen by session id.
     *
     * @param sessionId the session id
     * @return the screen, or `null` if it is not open
     */
    fun find(sessionId: Int): Entry<T>? = stack.firstOrNull { it.sessionId == sessionId }

    /**
     * Opens a screen. With a parent that is open, the screens above the parent are closed and the
     * new screen goes on top of it. Without a parent, or with a parent that is not open, every
     * open screen is closed first. A screen already open under the same session id is closed
     * before.
     *
     * @param sessionId the session id of the new screen
     * @param parentSessionId the session id of the parent, or `null`
     * @param closable whether the player can close the new screen with Escape
     * @param content the content kept for the new screen
     * @return the screens closed by this call, from top to bottom
     */
    fun open(sessionId: Int, parentSessionId: Int?, closable: Boolean, content: T): List<Entry<T>> {
        val removed = mutableListOf<Entry<T>>()
        if (find(sessionId) != null && sessionId != parentSessionId) removed += close(sessionId)
        val parentIndex = parentSessionId?.let { parent -> stack.indexOfFirst { it.sessionId == parent } } ?: -1
        removed += if (parentIndex >= 0) removeAbove(parentIndex) else closeAll()
        stack.addLast(Entry(sessionId, closable, content))
        return removed
    }

    /**
     * Closes a screen together with every screen above it.
     *
     * @param sessionId the session id of the screen
     * @return the closed screens from top to bottom, or an empty list if the screen is not open
     */
    fun close(sessionId: Int): List<Entry<T>> {
        val index = stack.indexOfFirst { it.sessionId == sessionId }
        if (index < 0) return emptyList()
        return removeAbove(index - 1)
    }

    /**
     * Closes every screen.
     *
     * @return the closed screens, from top to bottom
     */
    fun closeAll(): List<Entry<T>> = removeAbove(-1)

    /**
     * Removes every screen above a position.
     *
     * @param index the position of the last screen to keep, or `-1` to remove every screen
     * @return the removed screens, from top to bottom
     */
    private fun removeAbove(index: Int): List<Entry<T>> {
        val removed = mutableListOf<Entry<T>>()
        while (stack.size > index + 1) removed += stack.removeLast()
        return removed
    }
}

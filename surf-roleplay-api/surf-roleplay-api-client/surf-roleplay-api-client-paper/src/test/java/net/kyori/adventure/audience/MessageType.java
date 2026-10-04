package net.kyori.adventure.audience;

/**
 * A test-only stand-in for the message type of older Adventure versions, which the Paper API
 * still references in method signatures but the Adventure version on the test classpath lacks.
 * It lets the tests create proxies of Paper interfaces such as the player.
 */
public enum MessageType {
    /**
     * A chat message.
     */
    CHAT,

    /**
     * A system message.
     */
    SYSTEM,
}

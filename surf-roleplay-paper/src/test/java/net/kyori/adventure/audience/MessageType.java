package net.kyori.adventure.audience;

/**
 * Stands in for the Adventure 4 type that paper-api's {@code Player} still references, so that
 * {@code Player} can be mocked in unit tests.
 */
public enum MessageType {
    /** A chat message. */
    CHAT,
    /** A system message. */
    SYSTEM
}

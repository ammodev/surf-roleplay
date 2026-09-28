package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The description of one kind of typed screen: its key and the schemas of its state and of the
 * actions it sends.
 *
 * @param S the state class
 * @param A the action class
 * @property key the snake_case key that identifies the screen type
 * @property stateSerializer the serializer of the screen's state
 * @property actionSerializer the serializer of the screen's actions
 * @throws IllegalArgumentException if [key] is not snake_case
 */
class ScreenType<S, A>(
    val key: String,
    val stateSerializer: KSerializer<S>,
    val actionSerializer: KSerializer<A>,
) {
    init {
        require(KEY_PATTERN.matches(key)) { "Screen type key '$key' is not snake_case" }
    }

    /**
     * Encodes a state of this screen type.
     *
     * @param state the state
     * @return the ProtoBuf bytes
     */
    fun encodeState(state: S): ByteArray = ProtoBuf.encodeToByteArray(stateSerializer, state)

    /**
     * Decodes a state of this screen type.
     *
     * @param bytes the ProtoBuf bytes
     * @return the state
     * @throws kotlinx.serialization.SerializationException if the bytes are not a valid state
     */
    fun decodeState(bytes: ByteArray): S = ProtoBuf.decodeFromByteArray(stateSerializer, bytes)

    /**
     * Encodes an action of this screen type.
     *
     * @param action the action
     * @return the ProtoBuf bytes
     */
    fun encodeAction(action: A): ByteArray = ProtoBuf.encodeToByteArray(actionSerializer, action)

    /**
     * Decodes an action of this screen type.
     *
     * @param bytes the ProtoBuf bytes
     * @return the action
     * @throws kotlinx.serialization.SerializationException if the bytes are not a valid action
     */
    fun decodeAction(bytes: ByteArray): A = ProtoBuf.decodeFromByteArray(actionSerializer, bytes)

    /**
     * Returns a readable description of this screen type, naming its key.
     *
     * @return the description
     */
    override fun toString(): String = "ScreenType($key)"

    /**
     * Holds the key pattern shared by every screen type.
     */
    private companion object {
        /**
         * The pattern every screen type key must match.
         */
        val KEY_PATTERN = Regex("[a-z0-9_]+")
    }
}

/**
 * The registry of every [ScreenType] of the roleplay protocol.
 */
object ScreenTypes {
    /**
     * The debug counter, a typed screen that shows a number and can raise or lower it.
     */
    val DEBUG_COUNTER: ScreenType<CounterState, CounterAction> =
        ScreenType("debug_counter", CounterState.serializer(), CounterAction.serializer())

    /**
     * Every screen type of the protocol.
     */
    val all: List<ScreenType<*, *>> = listOf(DEBUG_COUNTER)

    /**
     * The screen types keyed by their key.
     */
    private val typesByKey: Map<String, ScreenType<*, *>> = all.associateBy { it.key }

    init {
        check(typesByKey.size == all.size) { "Two screen types share a key" }
    }

    /**
     * Looks up a screen type by its key.
     *
     * @param key the key
     * @return the screen type, or `null` if no screen type has the key
     */
    fun byKey(key: String): ScreenType<*, *>? = typesByKey[key]
}

/**
 * The state of the debug counter screen.
 *
 * @property value the number the screen shows
 */
@Serializable
data class CounterState(
    @ProtoNumber(1) val value: Int = 0,
)

/**
 * An action of the debug counter screen.
 *
 * @property delta the amount by which the player asks to change the number
 */
@Serializable
data class CounterAction(
    @ProtoNumber(1) val delta: Int = 0,
)

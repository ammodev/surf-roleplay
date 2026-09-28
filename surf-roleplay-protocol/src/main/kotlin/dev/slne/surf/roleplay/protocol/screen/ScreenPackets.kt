package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * What a screen shows: a generic widget tree or a typed screen.
 */
@Serializable
sealed interface ScreenBody

/**
 * The body of a generic screen.
 *
 * @property root the root node of the screen's tree
 */
@Serializable
@SerialName("widgets")
data class WidgetScreenBody(
    @ProtoNumber(1) val root: ScreenNode,
) : ScreenBody

/**
 * The body of a typed screen, which the mod implements in code.
 *
 * @property typeKey the key of the screen's [ScreenType]
 * @property state the screen's state, encoded with its screen type
 */
@Serializable
@SerialName("typed")
class TypedScreenBody(
    @ProtoNumber(1) val typeKey: String,
    @ProtoNumber(2) val state: ByteArray,
) : ScreenBody

/**
 * Opens a screen for the player, on top of a parent screen or as the only open screen.
 *
 * Opening a screen without a parent closes every other open screen first.
 *
 * @property sessionId the id of the new screen session, unique for the player
 * @property parentSessionId the session the screen opens on top of, or `null` to replace every
 *           open screen
 * @property title the screen's title as component JSON
 * @property closable whether the player can close the screen with Escape
 * @property body what the screen shows
 * @property theme the name of the theme the screen is drawn with; unknown names draw the
 *           `default` theme
 * @property variant the light or dark variant of the theme
 * @property presentation how the screen is shown relative to the screens below it
 * @property sheetEdge the window edge a sheet is attached to; used only for
 *           [Presentation.SHEET]
 */
@Serializable
data class ScreenOpen(
    @ProtoNumber(1) val sessionId: Int,
    @ProtoNumber(2) val parentSessionId: Int? = null,
    @ProtoNumber(3) val title: String,
    @ProtoNumber(4) val closable: Boolean = true,
    @ProtoNumber(5) val body: ScreenBody,
    @ProtoNumber(6) val theme: String = "default",
    @ProtoNumber(7) val variant: ThemeVariant = ThemeVariant.DARK,
    @ProtoNumber(8) val presentation: Presentation = Presentation.SCREEN,
    @ProtoNumber(9) val sheetEdge: SheetEdge = SheetEdge.RIGHT,
) : Packet

/**
 * Changes the tree of an open generic screen.
 *
 * @property sessionId the session of the screen
 * @property operations the changes, applied in order
 */
@Serializable
data class ScreenPatch(
    @ProtoNumber(1) val sessionId: Int,
    @ProtoNumber(2) val operations: List<PatchOperation> = emptyList(),
) : Packet

/**
 * Replaces the state of an open typed screen.
 *
 * @property sessionId the session of the screen
 * @property state the new state, encoded with the screen's type
 */
@Serializable
class ScreenTypedUpdate(
    @ProtoNumber(1) val sessionId: Int,
    @ProtoNumber(2) val state: ByteArray,
) : Packet

/**
 * Closes an open screen together with every screen above it, or every open screen.
 *
 * @property sessionId the session to close, or `null` to close every open screen
 */
@Serializable
data class ScreenClose(
    @ProtoNumber(1) val sessionId: Int? = null,
) : Packet

/**
 * Reports that the player clicked a widget of a generic screen.
 *
 * @property sessionId the session of the screen
 * @property widgetId the id of the clicked widget
 * @property values the current value of every input widget of the screen
 */
@Serializable
data class ScreenWidgetAction(
    @ProtoNumber(1) val sessionId: Int,
    @ProtoNumber(2) val widgetId: String,
    @ProtoNumber(3) val values: List<InputValue> = emptyList(),
) : Packet

/**
 * The value of one input widget.
 *
 * Text inputs send their text, number inputs their number in decimal or an empty string when
 * empty, checkboxes `true` or `false`, and selects the value of the selected option or an empty
 * string when nothing is selected.
 *
 * @property widgetId the id of the input widget
 * @property value the value in its string form
 */
@Serializable
data class InputValue(
    @ProtoNumber(1) val widgetId: String,
    @ProtoNumber(2) val value: String,
)

/**
 * Reports an action of a typed screen.
 *
 * @property sessionId the session of the screen
 * @property action the action, encoded with the screen's type
 */
@Serializable
class ScreenTypedAction(
    @ProtoNumber(1) val sessionId: Int,
    @ProtoNumber(2) val action: ByteArray,
) : Packet

/**
 * Reports that the player closed a screen, which also closed every screen above it.
 *
 * @property sessionId the session of the closed screen
 */
@Serializable
data class ScreenClosed(
    @ProtoNumber(1) val sessionId: Int,
) : Packet

/**
 * Reports that the player changed the value of an input that asked for change events, or the
 * typed query of a combobox that asked for search events.
 *
 * @property sessionId the session of the screen
 * @property widgetId the id of the input
 * @property value the new value, in the string form of [InputValue.value]; for a search event,
 *           the current value
 * @property query the typed query for a search event, or `null` for a value change
 */
@Serializable
data class ScreenInputChange(
    @ProtoNumber(1) val sessionId: Int,
    @ProtoNumber(2) val widgetId: String,
    @ProtoNumber(3) val value: String,
    @ProtoNumber(4) val query: String? = null,
) : Packet

package dev.slne.surf.roleplay.fabric.settings

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir

/**
 * Tests for the client settings store.
 */
class SettingsStoreTest {

    /**
     * The directory that holds the settings files of a test.
     */
    @TempDir
    lateinit var dir: Path

    /**
     * Verifies that a missing file gives defaults and is not created.
     */
    @Test
    fun `missing file gives defaults and is not created`() {
        val store = SettingsStore(dir.resolve("surf-roleplay.json")).apply { load() }
        assertEquals(ClientSettings(), store.current)
        assertFalse(Files.exists(dir.resolve("surf-roleplay.json")))
    }

    /**
     * Verifies that an update is persisted and read back by a new store.
     */
    @Test
    fun `update writes the file and a new store reads it back`() {
        val file = dir.resolve("surf-roleplay.json")
        SettingsStore(file).apply { load(); update { it.copy(cursorKeyMode = KeyMode.TOGGLE) } }
        assertEquals(KeyMode.TOGGLE, SettingsStore(file).apply { load() }.current.cursorKeyMode)
    }

    /**
     * Verifies that an unparsable file is renamed and defaults are used.
     */
    @Test
    fun `broken file is renamed and defaults are used`() {
        val file = dir.resolve("surf-roleplay.json").also { Files.writeString(it, "{not json") }
        val store = SettingsStore(file).apply { load() }
        assertEquals(ClientSettings(), store.current)
        assertTrue(Files.exists(dir.resolve("surf-roleplay.json.broken")))
    }

    /**
     * Verifies that a value of the wrong type falls back to the default.
     */
    @Test
    fun `wrong type for a key falls back to its default`() {
        val file = dir.resolve("surf-roleplay.json").also { Files.writeString(it, """{"cursorKeyMode": 5}""") }
        assertEquals(KeyMode.HOLD, SettingsStore(file).apply { load() }.current.cursorKeyMode)
    }

    /**
     * Verifies that unknown keys are kept when the file is rewritten.
     */
    @Test
    fun `unknown keys survive a rewrite`() {
        val file = dir.resolve("surf-roleplay.json").also { Files.writeString(it, """{"radialKeyMode":"TOGGLE"}""") }
        SettingsStore(file).apply { load(); update { it.copy(cursorKeyMode = KeyMode.TOGGLE) } }
        assertTrue(Files.readString(file).contains("radialKeyMode"))
    }

    /**
     * Verifies that listeners are told about updates.
     */
    @Test
    fun `listeners hear updates`() {
        val store = SettingsStore(dir.resolve("s.json")).apply { load() }
        var heard: ClientSettings? = null
        store.onChange { heard = it }
        store.update { it.copy(cursorKeyMode = KeyMode.TOGGLE) }
        assertEquals(KeyMode.TOGGLE, heard?.cursorKeyMode)
    }

    /**
     * Verifies that a file that cannot be read is kept in place and defaults are used.
     */
    @Test
    fun `unreadable file is kept and defaults are used`() {
        val file = dir.resolve("surf-roleplay.json").also { Files.createDirectory(it) }
        val store = SettingsStore(file).apply { load() }
        assertEquals(ClientSettings(), store.current)
        assertTrue(Files.isDirectory(file))
        assertFalse(Files.exists(dir.resolve("surf-roleplay.json.broken")))
    }

    /**
     * Verifies which failures count as content errors.
     */
    @Test
    fun `only content failures are classified as content errors`() {
        assertTrue(SettingsStore.isContentError(com.google.gson.JsonSyntaxException("x")))
        assertTrue(SettingsStore.isContentError(IllegalStateException("not an object")))
        assertTrue(SettingsStore.isContentError(java.nio.charset.MalformedInputException(1)))
        assertFalse(SettingsStore.isContentError(java.io.IOException("locked")))
        assertFalse(SettingsStore.isContentError(java.nio.file.AccessDeniedException("f")))
    }
}

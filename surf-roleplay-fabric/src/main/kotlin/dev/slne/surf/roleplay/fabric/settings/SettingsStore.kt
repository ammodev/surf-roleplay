package dev.slne.surf.roleplay.fabric.settings

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import org.slf4j.LoggerFactory

/**
 * Loads and saves the [ClientSettings] in a JSON file. Keys the store does not know are kept when
 * the file is rewritten. Updates and listeners run on the calling thread.
 *
 * @param file the settings file
 */
class SettingsStore(private val file: Path) {

    private val log = LoggerFactory.getLogger(SettingsStore::class.java)
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val listeners = mutableListOf<(ClientSettings) -> Unit>()
    private var json = JsonObject()

    /**
     * The current settings.
     */
    var current: ClientSettings = ClientSettings()
        private set

    /**
     * Reads the settings file. A missing file gives the defaults and is not created; an unreadable
     * file is moved to the same name with the suffix `.broken` and the defaults are used. A key that
     * is missing or of the wrong type gets its default.
     */
    fun load() {
        json = JsonObject()
        current = ClientSettings()
        if (!Files.exists(file)) return
        try {
            val parsed = JsonParser.parseString(Files.readString(file)).asJsonObject
            json = parsed
            current = ClientSettings(cursorKeyMode = readKeyMode(parsed, "cursorKeyMode", KeyMode.HOLD))
        } catch (e: Exception) {
            log.warn("Could not read the settings file {}, using defaults", file, e)
            json = JsonObject()
            current = ClientSettings()
            try {
                Files.move(file, file.resolveSibling(file.fileName.toString() + ".broken"), StandardCopyOption.REPLACE_EXISTING)
            } catch (moveError: Exception) {
                log.warn("Could not move the broken settings file {}", file, moveError)
            }
        }
    }

    /**
     * Replaces the settings with the result of [transform], writes the file and tells the listeners.
     *
     * @param transform computes the new settings from the current ones
     */
    fun update(transform: (ClientSettings) -> ClientSettings) {
        current = transform(current)
        json.addProperty("cursorKeyMode", current.cursorKeyMode.name)
        save()
        listeners.toList().forEach { it(current) }
    }

    /**
     * Registers a listener that is called with the new settings after each update.
     *
     * @param listener the listener
     */
    fun onChange(listener: (ClientSettings) -> Unit) {
        listeners += listener
    }

    /**
     * Reads a [KeyMode] from a string value of [obj], or returns [default] when it is missing,
     * not a string or not a known mode.
     */
    private fun readKeyMode(obj: JsonObject, key: String, default: KeyMode): KeyMode {
        val element = obj.get(key)
        if (element == null || !element.isJsonPrimitive || !element.asJsonPrimitive.isString) return default
        return KeyMode.entries.firstOrNull { it.name == element.asString } ?: default
    }

    /**
     * Writes the kept JSON to the file through a temporary file, logging a warning on failure.
     */
    private fun save() {
        try {
            val parent = file.toAbsolutePath().parent
            if (parent != null) Files.createDirectories(parent)
            val tmp = file.resolveSibling(file.fileName.toString() + ".tmp")
            Files.writeString(tmp, gson.toJson(json))
            Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: Exception) {
            log.warn("Could not write the settings file {}", file, e)
        }
    }
}

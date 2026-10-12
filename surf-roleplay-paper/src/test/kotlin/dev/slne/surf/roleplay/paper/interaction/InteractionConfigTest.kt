package dev.slne.surf.roleplay.paper.interaction

import org.bukkit.configuration.file.YamlConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for reading the interaction settings from config.yml.
 */
class InteractionConfigTest {

    /**
     * Reads settings from YAML.
     *
     * @param yaml the YAML
     * @param warnings receives the warnings
     * @return the settings
     */
    private fun read(yaml: String, warnings: MutableList<String> = mutableListOf()) =
        InteractionConfig.from(YamlConfiguration().apply { loadFromString(yaml) }) { warnings += it }

    /**
     * Verifies the defaults when the section is missing.
     */
    @Test
    fun `defaults without a section`() {
        assertEquals(InteractionConfig(3.0, 10, false), read(""))
    }

    /**
     * Verifies that values are read and invalid ones fall back with a warning.
     */
    @Test
    fun `values are read and invalid ones fall back`() {
        assertEquals(InteractionConfig(4.0, 5, true), read("interaction: { default-range: 4.0, max-queries-per-second: 5, demo-entries: true }"))
        val warnings = mutableListOf<String>()
        assertEquals(InteractionConfig(3.0, 10, false), read("interaction: { default-range: 20, max-queries-per-second: 0 }", warnings))
        assertEquals(2, warnings.size)
    }
}

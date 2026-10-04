package dev.slne.surf.roleplay.paper.tablist

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for writing configuration snapshots.
 */
class ConfigSnapshotWriterTest {

    /**
     * Verifies that a write replaces the content and leaves no temporary file behind.
     */
    @Test
    fun `write replaces the file and leaves no temp file`() {
        val directory = Files.createTempDirectory("snapshot")
        val file = directory.resolve("config.yml")
        Files.writeString(file, "old")

        ConfigSnapshotWriter.write(file, "first")
        ConfigSnapshotWriter.write(file, "second")

        assertEquals("second", Files.readString(file))
        assertEquals(listOf("config.yml"), Files.list(directory).use { stream -> stream.map { it.fileName.toString() }.toList() })
    }
}

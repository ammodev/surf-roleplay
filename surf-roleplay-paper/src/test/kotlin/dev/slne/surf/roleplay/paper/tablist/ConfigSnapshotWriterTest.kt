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

        ConfigSnapshotWriter.write(file, "first", 1)
        ConfigSnapshotWriter.write(file, "second", 2)

        assertEquals("second", Files.readString(file))
        assertEquals(listOf("config.yml"), Files.list(directory).use { stream -> stream.map { it.fileName.toString() }.toList() })
    }

    /**
     * Verifies that a snapshot older than the last written one is skipped.
     */
    @Test
    fun `older snapshot does not overwrite a newer one`() {
        val file = Files.createTempDirectory("snapshot").resolve("config.yml")

        assertEquals(true, ConfigSnapshotWriter.write(file, "B", 2))
        assertEquals(false, ConfigSnapshotWriter.write(file, "A", 1))
        assertEquals(false, ConfigSnapshotWriter.write(file, "B again", 2))

        assertEquals("B", Files.readString(file))
    }
}

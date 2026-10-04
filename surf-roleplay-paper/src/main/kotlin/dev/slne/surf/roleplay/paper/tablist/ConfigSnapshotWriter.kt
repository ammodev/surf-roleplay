package dev.slne.surf.roleplay.paper.tablist

import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Writes a text snapshot to a file so that readers never see a partly written file.
 */
object ConfigSnapshotWriter {

    /**
     * Guards the writes so that concurrent snapshots are written one after the other.
     */
    private val lock = Any()

    /**
     * The highest sequence written per target.
     */
    private val lastWritten = HashMap<Path, Long>()

    /**
     * Writes the content to a temporary file next to the target and moves it over the target
     * atomically, falling back to a plain replacing move where the file system cannot move
     * atomically.
     *
     * A snapshot is skipped if a snapshot with a higher or equal sequence was already written to
     * the same target, so snapshots handed to different threads cannot overwrite newer content
     * with older content.
     *
     * @param target the file to replace
     * @param content the new content of the file
     * @param sequence the position of the snapshot; larger values are newer
     * @return whether the snapshot was written, `false` if a newer one had already been written
     * @throws java.io.IOException if the file cannot be written
     */
    fun write(target: Path, content: String, sequence: Long): Boolean {
        synchronized(lock) {
            val key = target.toAbsolutePath().normalize()
            if (sequence <= (lastWritten[key] ?: Long.MIN_VALUE)) return false
            val directory = target.toAbsolutePath().parent
            val temp = Files.createTempFile(directory, target.fileName.toString(), ".tmp")
            try {
                Files.writeString(temp, content, StandardCharsets.UTF_8)
                try {
                    Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
                } catch (exception: AtomicMoveNotSupportedException) {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
                }
            } finally {
                Files.deleteIfExists(temp)
            }
            lastWritten[key] = sequence
            return true
        }
    }
}

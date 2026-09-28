import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.ImageTranscoder
import org.apache.batik.transcoder.image.PNGTranscoder
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.zip.ZipFile
import javax.imageio.ImageIO

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("org.apache.xmlgraphics:batik-transcoder:1.19")
        classpath("org.apache.xmlgraphics:batik-codec:1.19")
    }
}

/** The pinned Lucide release whose icons the mod ships. */
val lucideVersion = "1.48.0"

/** The side length of one icon in the atlas, in pixels. */
val lucideCell = 64

/** The number of icons per atlas row. */
val lucideColumns = 64

/** Where the downloaded release and its licence are kept. */
val lucideDownloads = layout.buildDirectory.dir("lucide/$lucideVersion")

/** Where the atlas, index and licence are generated as mod resources. */
val lucideResources = layout.buildDirectory.dir("generated/lucide")

val downloadLucide by tasks.registering {
    description = "Downloads the pinned Lucide icon release and its licence."
    val target = lucideDownloads
    outputs.dir(target)
    doLast {
        val dir = target.get().asFile.apply { mkdirs() }
        val zip = dir.resolve("lucide-icons.zip")
        if (!zip.exists()) {
            URI("https://github.com/lucide-icons/lucide/releases/download/$lucideVersion/lucide-icons-$lucideVersion.zip")
                .toURL().openStream().use { input -> zip.outputStream().use { input.copyTo(it) } }
        }
        val license = dir.resolve("LICENSE")
        if (!license.exists()) {
            URI("https://raw.githubusercontent.com/lucide-icons/lucide/$lucideVersion/LICENSE")
                .toURL().openStream().use { input -> license.outputStream().use { input.copyTo(it) } }
        }
    }
}

val rasterizeLucide by tasks.registering {
    description = "Rasterises every Lucide icon into one white texture atlas with a name index and a contact sheet."
    dependsOn(downloadLucide)
    val source = lucideDownloads
    val target = lucideResources
    val sheet = layout.buildDirectory.file("lucide/contact-sheet.png")
    inputs.dir(source)
    outputs.dir(target)
    outputs.file(sheet)
    doLast {
        val zip = ZipFile(source.get().asFile.resolve("lucide-icons.zip"))
        val entries = zip.entries().toList().filter { it.name.startsWith("icons/") && it.name.endsWith(".svg") }.sortedBy { it.name }
        val rows = (entries.size + lucideColumns - 1) / lucideColumns
        val atlas = BufferedImage(lucideColumns * lucideCell, rows * lucideCell, BufferedImage.TYPE_INT_ARGB)
        val index = StringBuilder("{\"version\":\"$lucideVersion\",\"cell\":$lucideCell,\"columns\":$lucideColumns,\"width\":${atlas.width},\"height\":${atlas.height},\"icons\":{")
        val aliasPattern = Regex("\"name\"\\s*:\\s*\"([a-z0-9-]+)\"")
        val categoryBlock = Regex("\"categories\"\\s*:\\s*\\[([^]]*)]")
        val failed = mutableListOf<String>()
        var first = true

        entries.forEachIndexed { position, entry ->
            val name = entry.name.removePrefix("icons/").removeSuffix(".svg")
            val svg = zip.getInputStream(entry).readBytes().toString(Charsets.UTF_8).replace("currentColor", "#ffffff")
            val image = try {
                rasterize(svg, lucideCell)
            } catch (exception: Exception) {
                failed += "$name: ${exception.message}"
                null
            }
            val column = position % lucideColumns
            val row = position / lucideColumns
            if (image != null) atlas.createGraphics().apply { drawImage(image, column * lucideCell, row * lucideCell, null); dispose() }

            val meta = zip.getEntry("icons/$name.json")?.let { zip.getInputStream(it).readBytes().toString(Charsets.UTF_8) } ?: ""
            val aliases = meta.substringAfter("\"aliases\"", "").let { part -> aliasPattern.findAll(part).map { it.groupValues[1] }.toList() }
            val categories = categoryBlock.find(meta)?.groupValues?.get(1)?.split(',')?.map { it.trim().trim('"') }?.filter { it.isNotEmpty() } ?: emptyList()
            for (key in listOf(name) + aliases) {
                if (!first) index.append(',')
                first = false
                val isAlias = key != name
                index.append("\"$key\":{\"x\":$column,\"y\":$row")
                if (isAlias) index.append(",\"alias\":\"$name\"") else index.append(",\"categories\":[${categories.joinToString(",") { "\"$it\"" }}]")
                index.append('}')
            }
        }
        index.append("}}")

        val out = target.get().asFile
        val textures = out.resolve("assets/surf-roleplay/textures/gui").apply { mkdirs() }
        ImageIO.write(atlas, "png", textures.resolve("lucide.png"))
        val meta = out.resolve("assets/surf-roleplay/lucide").apply { mkdirs() }
        meta.resolve("index.json").writeText(index.toString())
        source.get().asFile.resolve("LICENSE").copyTo(meta.resolve("LICENSE.txt"), overwrite = true)
        meta.resolve("failed.txt").writeText(failed.joinToString("\n"))

        val contact = BufferedImage(atlas.width, atlas.height, BufferedImage.TYPE_INT_RGB)
        contact.createGraphics().apply {
            color = Color(0x17, 0x17, 0x17)
            fillRect(0, 0, contact.width, contact.height)
            drawImage(atlas, 0, 0, null)
            dispose()
        }
        ImageIO.write(contact, "png", sheet.get().asFile)
        logger.lifecycle("Rasterised ${entries.size} Lucide icons into a ${atlas.width}x${atlas.height} atlas, ${failed.size} failed")
        zip.close()
    }
}

/**
 * Rasterises an SVG document into a square image.
 *
 * @param svg the SVG source
 * @param size the side length in pixels
 * @return the image
 */
fun rasterize(svg: String, size: Int): BufferedImage {
    var result: BufferedImage? = null
    val transcoder = object : ImageTranscoder() {
        override fun createImage(width: Int, height: Int) = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        override fun writeImage(image: BufferedImage, output: TranscoderOutput?) {
            result = image
        }
    }
    transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, size.toFloat())
    transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, size.toFloat())
    transcoder.transcode(TranscoderInput(ByteArrayInputStream(svg.toByteArray())), TranscoderOutput(ByteArrayOutputStream()))
    return result ?: error("no image produced")
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(rasterizeLucide)
    from(lucideResources)
}

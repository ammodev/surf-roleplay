plugins {
    id("net.fabricmc.fabric-loom") version "1.18.2"
    kotlin("jvm")
}

val minecraftVersion = "26.2"
val fabricLoaderVersion = "0.19.5"
val fabricApiVersion = "0.161.0+26.2"
val fabricLanguageKotlinVersion = "1.14.1+kotlin.2.4.20"
val serializationVersion = "1.11.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(25)
}

loom {
    runs {
        register("localClient") {
            inherit(getByName("client"))
            configName = "Minecraft Client (localhost)"
            programArgs("--quickPlayMultiplayer", "localhost:25565")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    implementation("net.fabricmc:fabric-language-kotlin:$fabricLanguageKotlinVersion")

    implementation(projects.surfRoleplayProtocol)
    include(projects.surfRoleplayProtocol)
    include("org.jetbrains.kotlinx:kotlinx-serialization-protobuf-jvm:$serializationVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

val devClientOptions = tasks.register("devClientOptions") {
    description = "Turns the narrator and the music off in the options of the dev client before it starts."
    val options = layout.projectDirectory.file("run/options.txt").asFile
    doLast {
        val settings = mapOf("narrator" to "0", "soundCategory_music" to "0.0")
        val kept = if (options.exists()) options.readLines().filter { line -> settings.keys.none { line.startsWith("$it:") } } else emptyList()
        options.parentFile.mkdirs()
        options.writeText((kept + settings.map { (key, value) -> "$key:$value" }).joinToString("\n", postfix = "\n"))
    }
}

tasks.matching { it.name == "runClient" || it.name == "runLocalClient" }.configureEach {
    dependsOn(devClientOptions)
}

tasks.processResources {
    val properties = mapOf(
        "version" to project.version,
        "minecraftVersion" to minecraftVersion,
        "fabricLoaderVersion" to fabricLoaderVersion,
    )
    inputs.properties(properties)
    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

apply(from = "lucide.gradle.kts")

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

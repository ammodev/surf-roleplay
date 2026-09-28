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

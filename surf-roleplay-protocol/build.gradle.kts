plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-protobuf-jvm:1.11.0") {
        isTransitive = false
    }

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
}

tasks.test {
    useJUnitPlatform()
}

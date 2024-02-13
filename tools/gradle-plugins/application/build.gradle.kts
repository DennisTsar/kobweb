plugins {
    `kotlin-dsl`
    id("com.varabyte.kobweb.internal.publish")
    alias(libs.plugins.kotlinx.serialization)
}

group = "com.varabyte.kobweb.gradle"
version = libs.versions.kobweb.libs.get()

dependencies {
    // Get access to Kotlin multiplatform source sets
    implementation(kotlin("gradle-plugin"))

    implementation(libs.kotlinx.serialization.json)

    // Common Gradle plugin used by Library, Application, and Worker plugins
    api(projects.tools.gradlePlugins.core)

    // For generating code / html
    implementation(libs.kotlinpoet)
    api(libs.kotlinx.html) // Exposed as api dependency because it's exposed by the kobweb.app.index API anyway.

    // Export
    implementation(libs.playwright)
    implementation(libs.jsoup)

    implementation("com.varabyte.kobweb:kobweb-common")
}

val DESCRIPTION = "A Gradle plugin that completes a user's Kobweb app"
gradlePlugin {
    plugins {
        create("kobwebApplication") {
            id = "com.varabyte.kobweb.application"
            displayName = "Kobweb Application Plugin"
            description = DESCRIPTION
            implementationClass = "com.varabyte.kobweb.gradle.application.KobwebApplicationPlugin"
        }
    }
}

kobwebPublication {
    // Leave artifactId blank. It will be set to the name of this module, and then the gradlePlugin step does some
    // additional tweaking that we don't want to interfere with.
    description.set(DESCRIPTION)
}

val serverJar by configurations.registering {
    isCanBeConsumed = false
    isTransitive = false
}
dependencies {
    @Suppress("UnstableApiUsage")
    serverJar("com.varabyte.kobweb.server:server") {
        targetConfiguration = "shadow"
    }
}

/**
 * Embed a copy of the latest Kobweb server, naming it server.jar and putting it into the project's resources/ dir, so
 * we can run it from the plugin at runtime.
 */
val copyServerJar by tasks.registering(Sync::class) {
    from(serverJar)
    into(layout.buildDirectory.dir("generated/kobweb/server"))
    rename("server-${libs.versions.kobweb.libs.get()}-all.jar", "server.jar")
}

kotlin.sourceSets.main {
    resources.srcDir(copyServerJar)
}

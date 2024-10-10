import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("org.jetbrains.dokka")
}

// Gradle is supposed to generate a `dokka {}` accessor, but sometimes it doesn't (?), so we use `configure {}` instead
configure<DokkaExtension> {
    dokkaSourceSets.configureEach {
        sourceLink {
            val path = projectDir.relativeTo(rootProject.projectDir).invariantSeparatorsPath

            localDirectory = projectDir.resolve("src")
            remoteUrl("https://github.com/varabyte/kobweb/tree/main/$path/src")
            remoteLineSuffix = "#L"
        }
    }
}

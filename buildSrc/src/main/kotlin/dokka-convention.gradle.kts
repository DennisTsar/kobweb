import gradle.kotlin.dsl.accessors._970e5eb52742fb615d1693cfc4f6fb41.dokka

plugins {
    id("org.jetbrains.dokka")
}

dokka {
    dokkaSourceSets.configureEach {
        sourceLink {
            val path = projectDir.relativeTo(rootProject.projectDir).invariantSeparatorsPath

            localDirectory.set(file("src"))
            remoteUrl("https://github.com/varabyte/kobweb/tree/main/$path/src")
            remoteLineSuffix.set("#L")
        }
    }
}

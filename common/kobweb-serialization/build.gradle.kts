import com.varabyte.kobweb.gradle.publish.setForMultiplatform

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    id("com.varabyte.kobweb.internal.publish")
    id("com.varabyte.kobweb.convention-base")
}

group = "com.varabyte.kobweb"
version = libs.versions.kobweb.libs.get()

kotlin {
    jvm()
    js {
        browser()
    }
}

kobwebPublication {
    artifactId.setForMultiplatform("kobweb-serialization")
    description.set("A collection of utility classes related to serialization.")
}

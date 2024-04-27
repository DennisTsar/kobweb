import com.varabyte.kobweb.gradle.publish.FILTER_OUT_MULTIPLATFORM_PUBLICATIONS
import com.varabyte.kobweb.gradle.publish.set

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    //alias(libs.plugins.jetbrains.compose)
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.varabyte.kobweb.internal.publish")
}

group = "com.varabyte.kobweb"
version = libs.versions.kobweb.libs.get()

kotlin {
    js {
        browser()
    }

    sourceSets {
        jsMain.dependencies {
            implementation("org.jetbrains.compose.runtime:runtime:1.6.10-beta02")
            implementation("org.jetbrains.compose.html:html-core:1.6.10-beta02")
            implementation(libs.kotlinx.serialization.json)
            api(projects.frontend.composeHtmlExt)
            implementation(projects.common.clientServerInternal)
            api(projects.frontend.kobwebWorkerInterface)
        }

        jsTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.truthish)
        }
    }
}

kobwebPublication {
    artifactId.set("kobweb-core")
    description.set("An opinionated framework making it easy to build web apps, leveraging Kotlin and Compose")
    filter.set(FILTER_OUT_MULTIPLATFORM_PUBLICATIONS)
}

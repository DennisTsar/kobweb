import com.varabyte.kobweb.gradle.publish.FILTER_OUT_MULTIPLATFORM_PUBLICATIONS
import com.varabyte.kobweb.gradle.publish.set

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
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
            implementation(compose.runtime)
            implementation(compose.html.core)

            implementation(projects.frontend.kobwebCore)
            api(projects.frontend.silkWidgets)
        }
    }
}

kobwebPublication {
    artifactId.set("silk-widgets-kobweb")
    description.set("Silk UI components tightly integrated with Kobweb functionality -- they cannot be used without Kobweb")
    filter.set(FILTER_OUT_MULTIPLATFORM_PUBLICATIONS)
}

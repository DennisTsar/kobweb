import com.varabyte.kobweb.gradle.library.util.configAsKobwebLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    //alias(libs.plugins.jetbrains.compose)
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.varabyte.kobweb.library")
}

group = "playground"
version = "1.0-SNAPSHOT"

kobweb {
    kspProcessorDependency.set("com.varabyte.kobweb:site-processors")
}

kotlin {
    configAsKobwebLibrary(includeServer = true)

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.runtime:runtime:1.6.10-beta02")
        }
        jsMain.dependencies {
            implementation("org.jetbrains.compose.html:html-core:1.6.10-beta02")
            implementation("com.varabyte.kobweb:kobweb-core")
            implementation("com.varabyte.kobweb:kobweb-silk")
            implementation("com.varabyte.kobwebx:silk-icons-fa")
        }
        jvmMain.dependencies {
            implementation("com.varabyte.kobweb:kobweb-api")
        }
    }
}

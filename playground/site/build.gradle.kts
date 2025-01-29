import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import com.varabyte.kobwebx.gradle.markdown.handlers.SilkCalloutBlockquoteHandler

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    id("com.varabyte.kobweb.application")
    id("com.varabyte.kobwebx.markdown")
    alias(libs.plugins.kotlinx.serialization)
    id("org.jetbrains.kotlinx.rpc.platform") version "0.5.0" // with this breaks at runtime
    id("org.jetbrains.kotlinx.rpc.plugin") version "0.5.0" // neede but breaks at compile time
}

group = "playground"
version = "1.0-SNAPSHOT"

kobweb {
    markdown {
        imports.add(".components.widgets.*")
        process.set { markdownEntries ->
            generateMarkdown("markdown/listing.md", buildString {
                appendLine("# Listing Index")
                markdownEntries.forEach { entry ->
                    appendLine("* [${entry.filePath}](${entry.route})")
                }
            })
        }
        handlers.blockquote.set(SilkCalloutBlockquoteHandler(labels = mapOf("QUOTE" to "")))
    }
    kspProcessorDependency.set("com.varabyte.kobweb:site-processors")
}

kotlin {
    configAsKobwebApplication(includeServer = true)

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-rpc-core")
            implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-serialization-json")
        }
        jsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.html.core)
            implementation(libs.kotlinx.serialization.json)
            implementation("com.varabyte.kobweb:kobweb-core")
            implementation("com.varabyte.kobweb:kobweb-silk")
            implementation("com.varabyte.kobwebx:silk-icons-fa")
            implementation("com.varabyte.kobwebx:kobwebx-markdown")
            implementation("com.varabyte.kobwebx:kobwebx-serialization-kotlinx")
            implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-client")
            implementation(project(":sitelib"))
            implementation(project(":worker"))
        }
        jvmMain.dependencies {
            implementation("org.slf4j:slf4j-api:2.0.13") // TODO: This probably shouldn't be required?
            implementation("com.varabyte.kobweb:kobweb-api")
            implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-server")
            implementation(project(":sitelib"))
        }
    }
}

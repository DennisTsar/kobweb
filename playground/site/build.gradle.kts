import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    //alias(libs.plugins.jetbrains.compose)
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.varabyte.kobweb.application")
    id("com.varabyte.kobwebx.markdown")
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
    }
    kspProcessorDependency.set("com.varabyte.kobweb:site-processors")
}

kotlin {
    configAsKobwebApplication(includeServer = true)
//    js {
//        compilerOptions.target = "es2015"
//    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.runtime:runtime:1.6.10-beta02")
        }
        jsMain {
            dependencies {
                implementation("org.jetbrains.compose.html:html-core:1.6.10-beta02")
                implementation("com.varabyte.kobweb:kobweb-core")
                implementation("com.varabyte.kobweb:kobweb-silk")
                implementation("com.varabyte.kobwebx:silk-icons-fa")
                implementation("com.varabyte.kobwebx:kobwebx-markdown")
                implementation(project(":sitelib"))
                implementation(project(":worker"))
            }
        }
        jvmMain.dependencies {
            implementation("com.varabyte.kobweb:kobweb-api")
            implementation(project(":sitelib"))
        }
    }
}

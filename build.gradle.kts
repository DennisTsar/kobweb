import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Plugins declared here instead of settings.gradle.kts because otherwise I get an error saying the kotlin plugin was
// applied multiple times.
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    `kotlin-dsl` apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    id("dokka-convention") // for aggregation
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dokka {
    moduleName.set("Kobweb")
}

dependencies {
    // TODO: remove undesired modules
    dokka(project(":common:kobweb-common"))
    dokka(project(":common:kobweb-serialization"))
    dokka(project(":common:kobwebx-serialization-kotlinx"))
    dokka(project(":common:client-server-internal"))
    dokka(project(":frontend:kobweb-core"))
    dokka(project(":frontend:kobweb-compose"))
    dokka(project(":frontend:kobweb-silk"))
    dokka(project(":frontend:kobweb-worker"))
    dokka(project(":frontend:kobweb-worker-interface"))
    dokka(project(":frontend:silk-foundation"))
    dokka(project(":frontend:silk-widgets"))
    dokka(project(":frontend:silk-widgets-kobweb"))
    dokka(project(":frontend:silk-icons-fa"))
    dokka(project(":frontend:silk-icons-mdi"))
    dokka(project(":frontend:kobwebx-markdown"))
    dokka(project(":frontend:compose-html-ext"))
    dokka(project(":frontend:browser-ext"))
//    dokka(project(":frontend:test:compose-test-utils"))
    dokka(project(":backend:kobweb-api"))
    dokka(project(":backend:server"))
    dokka(project(":backend:server-plugin"))
    dokka(project(":tools:gradle-plugins:core"))
    dokka(project(":tools:gradle-plugins:library"))
    dokka(project(":tools:gradle-plugins:application"))
    dokka(project(":tools:gradle-plugins:worker"))
    dokka(project(":tools:gradle-plugins:extensions:markdown"))
    dokka(project(":tools:ksp:site-processors"))
    dokka(project(":tools:ksp:worker-processor"))
    dokka(project(":tools:ksp:ksp-ext"))
    dokka(project(":tools:processor-common"))
}

subprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
            content {
                includeGroup("com.varabyte.truthish")
            }
        }
    }

    // Require Java 11 for a few APIs. A very important one is ProcessHandle, used for detecting if a
    // server is running in a cross-platform way.
    val jvmTarget = JvmTarget.JVM_11
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = jvmTarget.target
        targetCompatibility = jvmTarget.target
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(jvmTarget)
    }
}

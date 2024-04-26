plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    //alias(libs.plugins.jetbrains.compose) apply false
    id("com.varabyte.kobweb.application") apply false
    id("com.varabyte.kobweb.library") apply false
    id("com.varabyte.kobwebx.markdown") apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0-RC2-238" apply false
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
}

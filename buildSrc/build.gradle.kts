plugins {
    `kotlin-dsl`
}

group = "com.varabyte.kobweb.gradle"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.dokka.gradle.plugin)
    // Workaround for https://github.com/gradle/gradle/issues/27218 / https://github.com/adamko-dev/dokkatoo/issues/123
    implementation(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        create("publishKobwebArtifact") {
            id = "com.varabyte.kobweb.internal.publish"
            implementationClass = "com.varabyte.kobweb.gradle.publish.KobwebPublishPlugin"
        }
    }
}

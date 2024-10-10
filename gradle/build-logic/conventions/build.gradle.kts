plugins {
    `kotlin-dsl`
}

group = "com.varabyte.kobweb"

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    implementation(libs.dokka.gradle.plugin)
}

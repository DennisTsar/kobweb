plugins {
    `kotlin-dsl`
}

group = "com.varabyte.kobweb"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
}

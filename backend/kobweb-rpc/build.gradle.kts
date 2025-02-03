plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.varabyte.kobweb.internal.publish")
}

group = "com.varabyte.kobweb"
version = libs.versions.kobweb.get()

dependencies {
    implementation(projects.backend.kobwebApi) // TODO: api?
    implementation(libs.kotlinx.rpc) // TODO: api?
}

kobwebPublication {
    artifactName.set("Kobweb RPC")
    artifactId.set("kobweb-rpc")
    description.set("") // TODO
}

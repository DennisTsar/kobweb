plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.varabyte.kobweb.internal.publish")
}

group = "com.varabyte.kobweb"
version = libs.versions.kobweb.get()

kotlin {
    jvm()
    js {
        browser()
    }

    sourceSets {
        // TODO: api dependencies?
        jsMain.dependencies {
            implementation(libs.kotlinx.rpc.client)
            implementation(projects.frontend.kobwebCore)
        }
        jvmMain.dependencies {
            implementation(projects.backend.kobwebApi)
            implementation(libs.kotlinx.rpc.server)
        }
    }
}

kobwebPublication {
    artifactName.set("Kobweb RPC")
    artifactId.set("kobweb-rpc")
    description.set("") // TODO
}

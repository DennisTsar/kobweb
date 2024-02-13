pluginManagement {
    includeBuild("../gradle/settings")
}

plugins {
    id("com.varabyte.kobweb.repositories")
}

rootProject.name = "common"

include(":client-server-models")
include(":kobweb-common")
include(":kobweb-serialization")
include(":kobwebx-serialization-kotlinx")

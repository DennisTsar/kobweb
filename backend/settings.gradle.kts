pluginManagement {
    includeBuild("../gradle/settings")
}

plugins {
    id("com.varabyte.kobweb.repositories")
}

rootProject.name = "backend"

include(":kobweb-api")
include(":server")
include(":server-plugin")

includeBuild("../common")

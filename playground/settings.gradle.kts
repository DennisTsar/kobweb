pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "playground"

includeBuild("../backend")
includeBuild("../common")
includeBuild("../frontend")
includeBuild("../tools")

include(":site")
include(":sitelib")
include(":worker")

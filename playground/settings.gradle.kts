pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val kotlinVersion = System.getenv("kotlin_version")
            if (kotlinVersion != null) {
                version("kotlin", kotlinVersion)
            }
        }
    }
}

rootProject.name = "playground"

includeBuild("../")

include(":site")
include(":sitelib")
include(":worker")

pluginManagement {
    includeBuild("../gradle/plugins")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    versionCatalogs.create("libs") {
        from(files("../gradle/libs.versions.toml"))
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
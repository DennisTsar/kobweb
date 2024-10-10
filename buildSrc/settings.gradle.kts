rootProject.name = "buildSrc" // set explicitly for caching purposes w/project accessors

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

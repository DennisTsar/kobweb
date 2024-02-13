plugins {
    id("com.gradle.enterprise") version ("3.15.1")
}

gradleEnterprise {
    if (System.getenv("CI") != null) {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}

rootProject.name = "kobweb"

includeBuild("backend")
includeBuild("common")
includeBuild("frontend")
includeBuild("tools")

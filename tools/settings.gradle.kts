pluginManagement {
    includeBuild("../gradle/settings")
}

plugins {
    id("com.varabyte.kobweb.repositories")
}

rootProject.name = "tools"

include(":gradle-plugins:core")
include(":gradle-plugins:library")
include(":gradle-plugins:application")
include(":gradle-plugins:worker")
include(":gradle-plugins:extensions:markdown")
include(":ksp:site-processors")
include(":ksp:worker-processor")
include(":ksp:ksp-ext")
include(":processor-common")

includeBuild("../common")
includeBuild("../backend") // application plugin needs server jar

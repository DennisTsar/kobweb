pluginManagement {
    includeBuild("../gradle/settings")
}

plugins {
    id("com.varabyte.kobweb.repositories")
}

rootProject.name = "frontend"

include(":kobweb-core")
include(":kobweb-compose")
include(":kobweb-silk")
include(":kobweb-worker")
include(":kobweb-worker-interface")
include(":silk-foundation")
include(":silk-widgets")
include(":silk-widgets-kobweb")
include(":silk-icons-fa")
include(":silk-icons-mdi")
include(":relocated:kobweb-silk-widgets")
include(":relocated:kobweb-silk-icons-fa")
include(":relocated:kobweb-silk-icons-mdi")
include(":kobwebx-markdown")
include(":compose-html-ext")
include(":browser-ext")

includeBuild("../common")

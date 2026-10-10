pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "qualcomm-incident-assistant"

include(":app")
include(":core:domain")
include(":core:contracts")
include(":core:workflow")
include(":feature:audio")
include(":feature:report")
include(":adapter:stt")
include(":adapter:llm")
include(":adapter:tts")
include(":adapter:decision")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// `./gradlew -Psdk.dir=/path/to/sdk ...` writes the path into local.properties. The Android Gradle
// Plugin reads sdk.dir only from that file (ANDROID_HOME is used only when it's missing), so this
// is the only way to override it from the command line.
providers.gradleProperty("sdk.dir").orNull?.let { sdkDir ->
    val localProps = file("local.properties")
    val entry = "sdk.dir=" + sdkDir.replace("\\", "\\\\").replace(":", "\\:")
    val lines = if (localProps.exists()) localProps.readLines() else emptyList()
    val updated = lines.filterNot { it.trimStart().startsWith("sdk.dir") } + entry
    if (updated != lines) localProps.writeText(updated.joinToString("\n", postfix = "\n"))
}

rootProject.name = "ProfileSwitcher"
include(":app")
 
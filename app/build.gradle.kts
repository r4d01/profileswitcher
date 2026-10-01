plugins {
    alias(libs.plugins.android.application)
}

base {
    // APK names: profileswitcher-debug.apk / profileswitcher-release.apk
    archivesName = "profileswitcher"
}

android {
    namespace = "com.github.r4d01.profileswitcher"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.github.r4d01.profileswitcher"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation(libs.junit)
}

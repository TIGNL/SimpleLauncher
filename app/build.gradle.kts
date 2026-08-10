plugins {
    id("com.android.application")
}

android {
    namespace = "com.simplelauncher"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.simplelauncher"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("simplelauncher.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "simple123"
            keyAlias = "simplelauncher"
            keyPassword = System.getenv("KEYSTORE_PASSWORD") ?: "simple123"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

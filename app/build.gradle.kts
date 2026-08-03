import java.io.File
import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.rithikrathan.simplewebview"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.rithikrathan.simplewebview"
        minSdk = 24
        targetSdk = 34
        versionCode = 7
        versionName = "2.1.3"
    }

    signingConfigs {
        create("release") {
            val keystoreB64 = System.getenv("KEYSTORE_BASE64")
            if (keystoreB64 != null) {
                val keystoreFile = File(System.getProperty("user.home"), "simplewebview-release.jks")
                keystoreFile.writeBytes(Base64.getDecoder().decode(keystoreB64))
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (System.getenv("KEYSTORE_BASE64") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.12.0")
    implementation("io.coil-kt:coil:2.7.0")
}

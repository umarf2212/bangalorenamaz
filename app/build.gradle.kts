import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "one.umar.namazrings"
    compileSdk = 36

    defaultConfig {
        applicationId = "one.umar.namazrings"
        minSdk = 26
        // API 36 keeps the first Play upload valid after 31 August 2026.
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    val environmentStoreFile = providers.environmentVariable("NAMAZ_UPLOAD_KEYSTORE").orNull
    val environmentPassword = providers.environmentVariable("NAMAZ_UPLOAD_PASSWORD").orNull
    val environmentAlias = providers.environmentVariable("NAMAZ_UPLOAD_ALIAS").orNull ?: "upload"
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use(keystoreProperties::load)
    }
    val releaseStoreFile = environmentStoreFile
        ?: keystoreProperties.getProperty("storeFile")
    val releaseStorePassword = environmentPassword
        ?: keystoreProperties.getProperty("storePassword")
    val releaseKeyAlias = if (environmentPassword != null) {
        environmentAlias
    } else {
        keystoreProperties.getProperty("keyAlias")
    }
    val releaseKeyPassword = environmentPassword
        ?: keystoreProperties.getProperty("keyPassword")
    val hasReleaseSigning = listOf(
        releaseStoreFile,
        releaseStorePassword,
        releaseKeyAlias,
        releaseKeyPassword,
    ).all { !it.isNullOrBlank() }

    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(requireNotNull(releaseStoreFile))
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) {
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

    // The web page and Android app intentionally consume the same JSON files.
    sourceSets["main"].assets.srcDir(rootProject.file("months"))
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

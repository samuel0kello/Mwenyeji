import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

val localProperties =
    Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }?.let {
            load(it.inputStream())
        }
    }

android {
    namespace = "com.samuelokello.mwenyeji"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.samuelokello.mwenyeji"
        minSdk = 24
        targetSdk = 36
        versionCode = 1010
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "MAPBOX_ACCESS_TOKEN",
            "\"${localProperties.getProperty("MAPBOX_ACCESS_TOKEN") ?: ""}\"",
        )

        resValue(
            "string",
            "mapbox_access_token",
            localProperties.getProperty("MAPBOX_ACCESS_TOKEN") ?: "",
        )
        buildConfigField(
            "String",
            "MAPBOX_DOWNLOADS_TOKEN",
            "\"${localProperties.getProperty("MAPBOX_DOWNLOADS_TOKEN") ?: ""}\"",
        )
    }

    signingConfigs {
        create("release") {
            storeFile = file(localProperties["STORE_FILE"] ?: "keystore.jks")
            storePassword = localProperties["STORE_PASSWORD"] as String? ?: ""
            keyAlias = localProperties["KEY_ALIAS"] as String? ?: ""
            keyPassword = localProperties["KEY_PASSWORD"] as String? ?: ""
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        create("beta") {
            initWith(getByName("release"))
            versionNameSuffix = "-beta"
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

//    koin
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.composable.core)
    implementation(libs.material)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)

    implementation(libs.play.services.basement)
    implementation(libs.play.services.location)

    implementation(libs.android.ndk27)
    implementation(libs.maps.compose.ndk27)
    implementation(libs.autofill.ndk27)
    implementation(libs.discover.ndk27)
    implementation(libs.place.autocomplete.ndk27)
    implementation(libs.offline.ndk27)
    implementation(libs.mapbox.search.android.ndk27)
    implementation(libs.mapbox.search.android.ui.ndk27)

    // ktor
    implementation(libs.bundles.ktor)
    implementation(libs.ktor.client.android)

    // play update
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)
}

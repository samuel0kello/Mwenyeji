package helpers

import com.android.build.api.dsl.CommonExtension
import extensions.ExtensionType
import extensions.androidApplication
import extensions.androidExtension
import extensions.androidLibrary
import extensions.configureDefaultConfig
import extensions.kotlinOptions
import extensions.libs
import extensions.loadProperties
import org.gradle.api.JavaVersion
import org.gradle.api.Project

internal fun Project.configureKotlinAndroid(extension: ExtensionType) {
    configureDefaultConfig(extension)
    androidExtension {
        compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()

        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }

    val localProperties = loadProperties("local.properties")

    when (extension) {
        ExtensionType.APPLICATION -> {
            androidApplication {
                buildTypes {
                    debug {
                        versionNameSuffix = "-debug"
                        applicationIdSuffix = ".debug"
                        isDebuggable = true
                    }

                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        isDebuggable = false
                        signingConfig = signingConfigs.getByName("release")
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }

                    signingConfigs {
                        maybeCreate("release").apply {
                            storeFile = localProperties.getProperty("STORE_FILE")?.let { file(it) } ?: file("keystore.jks")
                            storePassword = localProperties.getProperty("STORE_PASSWORD") ?: ""
                            keyAlias = localProperties.getProperty("KEY_ALIAS") ?: ""
                            keyPassword = localProperties.getProperty("KEY_PASSWORD") ?: ""
                        }
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

                buildFeatures {
                    buildConfig = true
                    resValues = true
                    mlModelBinding = true
                }
            }
        }

        ExtensionType.LIBRARY -> {
            androidLibrary {
                buildFeatures {
                    buildConfig = true
                }
            }
        }
    }
}

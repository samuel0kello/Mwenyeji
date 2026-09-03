package extensions

import org.gradle.api.Project

fun Project.configureDefaultConfig(extensionType: ExtensionType) {
    val versionProperties = loadProperties("versions.properties")

    when (extensionType) {
        ExtensionType.APPLICATION -> {
            androidApplication {
                defaultConfig {
                    applicationId = "com.samuelokello.mwenyeji"
                    minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
                    targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
                    versionCode = versionProperties.getProperty("versionCode", "1").toIntOrNull() ?: 1
                    versionName = versionProperties.getProperty("versionName", "1.0.0.0")
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    multiDexEnabled = true
                }
            }
        }

        ExtensionType.LIBRARY -> {
            androidLibrary {
                defaultConfig {
                    minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
                    multiDexEnabled = true

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    consumerProguardFiles("consumer-rules.pro")
                }
            }
        }
    }
}

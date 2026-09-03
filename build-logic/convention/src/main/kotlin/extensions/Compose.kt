package extensions

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies


fun Project.configureCompose(extensionType: ExtensionType) {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

    when(extensionType){
        ExtensionType.APPLICATION -> {
            androidApplication {
                buildFeatures.compose = true
            }
        }
        ExtensionType.LIBRARY -> {
            androidLibrary {
                buildFeatures.compose = true
            }
        }
    }

    val bom = libs.findLibrary("androidx-compose-bom").get()
    dependencies {
        implementation(platform(bom))
        implementation(libs.findLibrary("androidx-compose-ui").get())
        implementation(libs.findLibrary("androidx-compose-ui-graphics").get())
        implementation(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        implementation(libs.findLibrary("androidx-material3").get())
        androidTestImplementation(platform(bom))
        androidTestImplementation(libs.findLibrary("androidx-compose-ui-test-junit4").get())
        debugImplementation(libs.findLibrary("androidx-compose-ui-tooling").get())
        debugImplementation(libs.findLibrary("androidx-compose-ui-test-manifest").get())
    }
}

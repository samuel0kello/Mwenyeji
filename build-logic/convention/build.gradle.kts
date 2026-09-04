plugins {
    `kotlin-dsl`
}

group = "com.samuelokello.build-logic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "com.samuelokello.build-logic.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "com.samuelokello.build-logic.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }

        register("androidCompose") {
            id = "com.samuelokello.build-logic.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
    }
}

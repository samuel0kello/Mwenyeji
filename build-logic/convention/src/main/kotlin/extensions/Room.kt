package extensions

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

fun Project.configureRoom() {
    dependencies {
        "implementation"(libs.findLibrary("androidx-room-runtime").get())
        "implementation"(libs.findLibrary("androidx-room-ktx").get())
        "ksp"(libs.findLibrary("androidx-room-compiler").get())
    }
}

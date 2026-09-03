package extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.PluginManager
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import java.util.Properties

internal fun Project.kotlinOptions(block: KotlinAndroidProjectExtension.() -> Unit) {
    extensions.configure<KotlinAndroidProjectExtension>(block)
}

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.loadProperties(fileName: String): Properties =
    Properties().apply {
        val propertiesFile = rootProject.file(fileName)
        if (propertiesFile.exists()) propertiesFile.inputStream().use { load(it) }
    }

internal fun Project.androidExtension(block: CommonExtension.() -> Unit) {
    extensions.configure(CommonExtension::class.java, block)
}
internal fun Project.androidLibrary(block: LibraryExtension.() -> Unit) {
    extensions.configure<LibraryExtension> { block() }
}

internal fun Project.androidApplication(block: ApplicationExtension.() -> Unit) {
    extensions.configure<ApplicationExtension> { block() }
}

fun DependencyHandlerScope.implementation(dependencyNotation: Any) {
    add("implementation", dependencyNotation)
}

fun DependencyHandlerScope.androidTestImplementation(dependencyNotation: Any) {
    add("androidTestImplementation", dependencyNotation)
}

fun DependencyHandlerScope.debugImplementation(dependencyNotation: Any) {
    add("debugImplementation", dependencyNotation)
}

enum class ExtensionType {
    APPLICATION,
    LIBRARY,
}

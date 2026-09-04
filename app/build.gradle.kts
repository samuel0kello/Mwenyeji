import java.util.Properties

plugins {
    id("com.samuelokello.build-logic.application")
    id("com.samuelokello.build-logic.compose")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "com.samuelokello.mwenyeji"

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
        mlModelBinding = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {

    // Core Compose & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    // MATERIAL 3 EXPRESSIVE

    //  UI Foundation
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.googleid)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.kotlinx.serialization.json)

//    koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.bundles.koin)

    implementation(libs.androidx.datastore.preferences)

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

    // ML & TFLite
    implementation(libs.play.services.tflite.java)
    implementation(libs.tensorflow.lite)

    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.tensorflow.lite.metadata)

    // kotlinDL
    implementation(libs.kotlin.deeplearning.api)
}

// Define bumper tasks

abstract class VersionBumperTask : DefaultTask() {
    @get:Internal
    abstract val versionFile: RegularFileProperty

    @get:Internal
    abstract val projectDirectory: DirectoryProperty

    @get:Internal
    abstract val bumpType: Property<String>

    @TaskAction
    fun bump() {
        val propertiesFile = versionFile.get().asFile
        val workingDir = projectDirectory.get().asFile
        val type = bumpType.get()

        if (!propertiesFile.exists()) {
            propertiesFile.createNewFile()
            propertiesFile.writeText("versionCode=1\nversionName=1.0.0.0")
        }

        val properties = Properties().apply {
            propertiesFile.inputStream().use { load(it) }
        }

        val currentVersionName = properties.getProperty("versionName", "1.0.0.0")
        val currentVersionCode = properties.getProperty("versionCode", "1").toInt()

        val cleanVersionName = currentVersionName.split("-")[0]
        val parts = cleanVersionName.split(".")

        val major = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
        val hotfix = parts.getOrNull(3)?.toIntOrNull() ?: 0

        val newVersionName = when (type) {
            "major" -> "${major + 1}.0.0.0"
            "minor" -> "$major.${minor + 1}.0.0"
            "patch" -> "$major.$minor.${patch + 1}.0"
            "hotfix" -> "$major.$minor.$patch.${hotfix + 1}"
            else -> throw IllegalArgumentException("Unknown bump type: $type")
        }
        val newVersionCode = currentVersionCode + 1

        properties["versionName"] = newVersionName
        properties["versionCode"] = newVersionCode.toString()

        propertiesFile.outputStream().use { properties.store(it, null) }

        val addExitCode = ProcessBuilder("git", "add", propertiesFile.absolutePath)
            .directory(workingDir)
            .inheritIO()
            .start()
            .waitFor()

        if (addExitCode != 0) {
            println("❌ 'git add' failed with exit code $addExitCode")
            return
        }

        val commitExitCode = ProcessBuilder("git", "commit", "-m", "Bump version to $newVersionName ($newVersionCode)")
            .directory(workingDir)
            .inheritIO()
            .start()
            .waitFor()

        if (commitExitCode != 0) {
            println("⚠️ 'git commit' failed with exit code $commitExitCode. You might need to commit manually.")
        } else {
            println("✅ Version bumped to $newVersionName (code: $newVersionCode)")
        }
    }
}

tasks.register<VersionBumperTask>("bumperVersionMajor") {
    versionFile.set(rootProject.layout.projectDirectory.file("versions.properties"))
    projectDirectory.set(project.layout.projectDirectory)
    bumpType.set("major")
}

tasks.register<VersionBumperTask>("bumperVersionMinor") {
    versionFile.set(rootProject.layout.projectDirectory.file("versions.properties"))
    projectDirectory.set(project.layout.projectDirectory)
    bumpType.set("minor")
}

tasks.register<VersionBumperTask>("bumperVersionPatch") {
    versionFile.set(rootProject.layout.projectDirectory.file("versions.properties"))
    projectDirectory.set(project.layout.projectDirectory)
    bumpType.set("patch")
}

tasks.register<VersionBumperTask>("bumperVersionHotFix") {
    versionFile.set(rootProject.layout.projectDirectory.file("versions.properties"))
    projectDirectory.set(project.layout.projectDirectory)
    bumpType.set("hotfix")
}

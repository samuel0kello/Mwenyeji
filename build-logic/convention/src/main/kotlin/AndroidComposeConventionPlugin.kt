import extensions.ExtensionType
import extensions.configureCompose
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extensionType = if (pluginManager.hasPlugin("com.android.application")) {
                ExtensionType.APPLICATION
            } else {
                ExtensionType.LIBRARY
            }
            configureCompose(extensionType)
        }
    }
}

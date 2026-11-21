import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

class ConventionsPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            val kotlinVersion = getKotlinPluginVersion()
            val agp = libs.findLibrary("agp-gradle-api").get().get().module
            val agpVersion = libs.findVersion("google-agp").get().requiredVersion
            configurations.matching { it.name != "ktlint" }.configureEach { configuration ->
                configuration.resolutionStrategy.eachDependency { details ->
                    if (details.requested.group == "org.jetbrains.kotlin" && details.requested.name.startsWith("kotlin")) {
                        details.useVersion(kotlinVersion)
                    }
                    if (details.requested.group == agp.group && details.requested.module.name == agp.name) {
                        details.useVersion(agpVersion)
                    }
                }
            }
        }
    }
}

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugins.signing.SigningExtension

class PublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("maven-publish")
        if (findConfig("SIGNING_PASSWORD").isNotEmpty()) {
            pluginManager.apply("signing")
        }

        extensions.configure<JavaPluginExtension> {
            withSourcesJar()
        }
        extensions.configure<PublishingExtension> {
            with(repositories) {
                maven { maven ->
                    maven.name = "github"
                    maven.setUrl("https://maven.pkg.github.com/usefulness/easylauncher-gradle-plugin")
                    with(maven.credentials) {
                        username = "mateuszkwiecinski"
                        password = findConfig("GITHUB_TOKEN")
                    }
                }
            }
        }
        pluginManager.withPlugin("com.gradle.plugin-publish") {
            extensions.configure<GradlePluginDevelopmentExtension>("gradlePlugin") { gradlePlugin ->
                gradlePlugin.apply {
                    website.set("https://github.com/usefulness/easylauncher-gradle-plugin")
                    vcsUrl.set("https://github.com/usefulness/easylauncher-gradle-plugin")
                }
            }
        }

        pluginManager.withPlugin("signing") {
            with(extensions.extraProperties) {
                set("signing.keyId", findConfig("SIGNING_KEY_ID"))
                set("signing.password", findConfig("SIGNING_PASSWORD"))
                set("signing.secretKeyRingFile", findConfig("SIGNING_SECRET_KEY_RING_FILE"))
            }

            extensions.configure<SigningExtension>("signing") { signing ->
                signing.sign(extensions.getByType(PublishingExtension::class.java).publications)
            }
        }
    }


    private inline fun <reified T> ExtensionContainer.configure(crossinline receiver: T.() -> Unit) {
        configure(T::class.java) { receiver(it) }
    }
}

private fun Project.findConfig(key: String): String {
    return findProperty(key)?.toString() ?: System.getenv(key) ?: ""
}

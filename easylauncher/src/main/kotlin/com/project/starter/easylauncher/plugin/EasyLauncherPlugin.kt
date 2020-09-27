package com.project.starter.easylauncher.plugin

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

class EasyLauncherPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val extension = extensions.create(EasyLauncherExtension.NAME, EasyLauncherExtension::class.java)

        logger.info("Running gradle version: ${gradle.gradleVersion}")

        configureSupportedPlugins { variants ->
            val android = extensions.getByType(BaseExtension::class.java)

            val easyLauncherTasks = mutableListOf<TaskProvider<EasyLauncherTask>>()

            variants.configureEach { variant ->
                val configs = extension.variants.filter { it.name == variant.name }.takeIf { it.isNotEmpty() }
                    ?: findConfigs(variant, extension.productFlavors, extension.buildTypes)

                val enabled = configs.all { it.enabled.get() }

                if (enabled) {
                    val filters = configs.flatMap { it.filters.get() }.toMutableSet()

                    // set default ribbon
                    if (filters.isEmpty() && variant.buildType.isDebuggable) {
                        val ribbonText = when (extension.isDefaultFlavorNaming.orNull) {
                            true -> variant.flavorName
                            false -> variant.buildType.name
                            null ->
                                if (variant.productFlavors.isEmpty()) {
                                    variant.buildType.name
                                } else {
                                    variant.flavorName
                                }
                        }
                        filters.add(EasyLauncherConfig(ribbonText, project.objects).greenRibbonFilter())
                    }

                    if (filters.isNotEmpty()) {
                        val generatedResDir = getGeneratedResDir(variant)
                        android.sourceSets.getByName(variant.name).res.srcDir(generatedResDir)

                        val customIconNames = provider {
                            val global = extension.iconNames.orNull.orEmpty()
                            val variantSpecific = configs.flatMap { config -> config.iconNames.orNull.orEmpty() }
                            (global + variantSpecific).toSet()
                        }

                        val name = "${EasyLauncherTask.NAME}${variant.name.capitalize()}"
                        val task = tasks.register(name, EasyLauncherTask::class.java) {
                            it.variantName.set(variant.name)
                            it.outputDir.set(generatedResDir)
                            it.filters.set(filters)
                            it.iconsNames.set(customIconNames)
                        }

                        easyLauncherTasks.add(task)

                        tasks.named("generate${variant.name.capitalize()}Resources") { it.dependsOn(task) }
                    }
                } else {
                    logger.info("disabled for ${variant.name}")
                }
            }

            tasks.register(EasyLauncherTask.NAME) { it.dependsOn(easyLauncherTasks) }
        }
    }

    private fun findConfigs(
        variant: BaseVariant,
        ribbonProductFlavors: Iterable<EasyLauncherConfig>,
        ribbonBuildTypes: Iterable<EasyLauncherConfig>
    ): List<EasyLauncherConfig> =
        ribbonProductFlavors.filter { config -> variant.productFlavors.any { config.name == it.name } } +
            ribbonBuildTypes.filter { it.name == variant.buildType.name }

    private fun Project.getGeneratedResDir(variant: BaseVariant) =
        File(project.buildDir, "generated/easylauncher/res/${variant.name}")
}

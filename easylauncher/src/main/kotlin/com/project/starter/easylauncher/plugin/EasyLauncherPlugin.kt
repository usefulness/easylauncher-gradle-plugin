package com.project.starter.easylauncher.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

class EasyLauncherPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val extension = extensions.create(EasyLauncherExtension.NAME, EasyLauncherExtension::class.java)
        val ribbonVariants = container(EasyLauncherConfig::class.java)
        extensions.add("variants", ribbonVariants)
        val ribbonBuildTypes = container(EasyLauncherConfig::class.java)
        extensions.add("buildTypes", ribbonBuildTypes)
        val ribbonProductFlavors = container(EasyLauncherConfig::class.java)
        extensions.add("productFlavors", ribbonProductFlavors)

        pluginManager.withPlugin("com.android.application") {
            val android = extensions.getByType(AppExtension::class.java)

            val easyLauncherTasks = mutableListOf<TaskProvider<EasyLauncherTask>>()

            android.applicationVariants.configureEach { variant ->
                val configs = ribbonVariants.filter { it.name == variant.name }.takeIf { it.isNotEmpty() }
                    ?: findConfigs(variant, ribbonProductFlavors, ribbonBuildTypes)

                val enabled = configs.all { it.enabled }

                if (enabled) {
                    val filters = configs.flatMap { it.filters }.toMutableSet()

                    // set default ribbon
                    if (filters.isEmpty() && variant.buildType.isDebuggable) {
                        val ribbonText = if (extension.isDefaultFlavorNaming) {
                            variant.flavorName
                        } else {
                            variant.buildType.name
                        }
                        filters.add(EasyLauncherConfig(ribbonText).greenRibbonFilter())
                    }

                    if (filters.isNotEmpty()) {
                        val generatedResDir = getGeneratedResDir(variant)
                        android.sourceSets.getByName(variant.name).res.srcDir(generatedResDir)

                        val name = "${EasyLauncherTask.NAME}${variant.name.capitalize()}"
                        val task = tasks.register(name, EasyLauncherTask::class.java) {
                            it.variantName.set(variant.name)
                            it.outputDir.set(generatedResDir)
                            it.filters.set(filters)
                        }

                        easyLauncherTasks.add(task)

                        tasks.named("generate${variant.name.capitalize()}Resources") { it.dependsOn(task) }
                    }
                }
            }

            tasks.register(EasyLauncherTask.NAME) { it.dependsOn(easyLauncherTasks) }
        }
    }

    private fun findConfigs(
        variant: ApplicationVariant,
        ribbonProductFlavors: Iterable<EasyLauncherConfig>,
        ribbonBuildTypes: Iterable<EasyLauncherConfig>
    ): List<EasyLauncherConfig> =
        ribbonProductFlavors.filter { it.name == variant.flavorName } +
            ribbonBuildTypes.filter { it.name == variant.buildType.name }

    private fun Project.getGeneratedResDir(variant: ApplicationVariant) =
        File(project.buildDir, "generated/easylauncher/res/${variant.name}")
}

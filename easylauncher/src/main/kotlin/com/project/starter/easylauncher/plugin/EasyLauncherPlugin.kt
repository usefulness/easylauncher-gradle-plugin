package com.project.starter.easylauncher.plugin

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.project.starter.easylauncher.filter.EasyLauncherFilter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.Locale

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
                        val customIconNames = provider {
                            val global = extension.iconNames.orNull.orEmpty()
                            val variantSpecific = configs.flatMap { config -> config.iconNames.orNull.orEmpty() }
                            (global + variantSpecific).toSet()
                        }

                        val task = registerTask(
                            android = android,
                            variant = variant,
                            customIconNames = customIconNames,
                            filters = filters,
                        )

                        easyLauncherTasks.add(task)

                        tasks.named("generate${variant.name.capitalize(Locale.ROOT)}Resources") { it.dependsOn(task) }
                    }
                } else {
                    logger.info("disabled for ${variant.name}")
                }
            }

            tasks.register(EasyLauncherTask.NAME) { it.dependsOn(easyLauncherTasks) }
        }
    }

    private fun Project.registerTask(
        android: BaseExtension,
        variant: BaseVariant,
        customIconNames: Provider<Set<String>>,
        filters: Set<EasyLauncherFilter>,
    ): TaskProvider<EasyLauncherTask> {
        val generatedResDir = getGeneratedResDir(variant)
        android.sourceSets.getByName(variant.name).res.srcDir(generatedResDir)

        val icons = provider {
            val names = (customIconNames.get().takeIf { it.isNotEmpty() } ?: android.getLauncherIconNames(variant)).toSet()
            logger.info("will process icons: ${names.joinToString()}")

            variant.getAllResDirectories(except = generatedResDir).flatMap { resDir ->
                names.flatMap { objects.getIconFiles(parent = resDir, iconName = it) }
            }
        }
        val minSdkVersion = provider {
            (variant.mergedFlavor.minSdkVersion ?: android.defaultConfig.minSdkVersion)?.apiLevel ?: 1
        }

        val name = "${EasyLauncherTask.NAME}${variant.name.replaceFirstChar(Char::titlecase)}"

        return tasks.register(name, EasyLauncherTask::class.java) {
            it.outputDir.set(generatedResDir)
            it.filters.set(filters)
            it.minSdkVersion.set(minSdkVersion)
            it.icons.from(icons)
            it.resourceDirectories.from(variant.getAllResDirectories(except = generatedResDir))
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

    private fun BaseExtension.getLauncherIconNames(variant: BaseVariant) =
        getAndroidManifestFiles(variant)
            .flatMap { manifestFile -> manifestFile.getLauncherIcons(variant.mergedFlavor.manifestPlaceholders) }

    private fun BaseVariant.getAllResDirectories(except: File) =
        sourceSets.flatMap { sourceSet -> sourceSet.resDirectories }
            .filterNot { resDirectory -> resDirectory == except }

    private fun BaseExtension.getAndroidManifestFiles(variant: BaseVariant): Iterable<File> {
        return listOf("main", variant.name, variant.buildType.name, variant.flavorName)
            .filter { it.isNotEmpty() }
            .distinct()
            .map { name -> sourceSets.getByName(name).manifest.srcFile }
            .filter { it.exists() }
    }
}

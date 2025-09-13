package com.project.starter.easylauncher.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import java.io.File

class EasyLauncherPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val extension = extensions.create(EasyLauncherExtension.NAME, EasyLauncherExtension::class.java)

        val androidComponents = project.extensions.findByType(AndroidComponentsExtension::class.java)
            ?: error("'com.starter.easylauncher' has to be applied after Android Gradle Plugin")
        val agpVersion = androidComponents.pluginVersion
        log.info { "Environment: gradle=${gradle.gradleVersion}, agp=$agpVersion" }

        androidComponents.onVariants { variant ->
            val configs = extension.variants.filter { it.name == variant.name }.takeIf { it.isNotEmpty() }
                ?: findConfigs(variant, extension.productFlavors, extension.buildTypes)

            val enabled = configs.all { it.enabled.get() }

            if (enabled) {
                val filters = configs.flatMap { it.filters.get() }.toMutableSet()

                // set default ribbon
                if (filters.isEmpty() && variant.debuggable) {
                    val ribbonText = when (extension.isDefaultFlavorNaming.orNull) {
                        true -> variant.flavorName

                        false -> variant.buildType

                        null ->
                            if (variant.productFlavors.isEmpty()) {
                                variant.buildType
                            } else {
                                variant.flavorName
                            }
                    }

                    if (ribbonText != null) {
                        filters.add(EasyLauncherConfig(ribbonText, project.objects).greenRibbonFilter())
                    }
                }

                log.info { "configuring ${variant.name}, isDebuggable=${variant.debuggable}, filters=${filters.size}" }

                if (filters.isNotEmpty()) {
                    val customIconNames = provider {
                        val global = extension.iconNames.orNull.orEmpty()
                        val variantSpecific = configs.flatMap { config -> config.iconNames.orNull.orEmpty() }
                        (global + variantSpecific).toSet()
                    }

                    val manifests = variant.sources.manifests.all.map { manifests -> manifests.map { it.asFile } }

                    val resSourceDirectories = variant.sources.res?.static
                        ?.map { outer ->
                            outer.flatten()
                                .map { it.asFile }
                                .sortedWith(resDirectoriesComparator(variant, projectPath = layout.projectDirectory))
                        }
                        ?: project.provider { emptyList() }

                    val capitalisedVariantName = variant.name.replaceFirstChar(Char::titlecase)
                    val task = project.tasks.register("easylauncher$capitalisedVariantName", EasyLauncherTask::class.java) {
                        it.manifestFiles.set(manifests)
                        it.manifestPlaceholders.set(variant.manifestPlaceholders)
                        it.resourceDirectories.set(resSourceDirectories)
                        it.filters.set(filters)
                        it.customIconNames.set(customIconNames)
                        it.minSdkVersion.set(variant.minSdk.apiLevel)
                    }

                    variant.sources.res?.addGeneratedSourceDirectory(task, EasyLauncherTask::outputDir)
                }
            } else {
                log.info { "disabled for ${variant.name}" }
            }
        }
    }

    private fun findConfigs(
        variant: Variant,
        ribbonProductFlavors: Iterable<EasyLauncherConfig>,
        ribbonBuildTypes: Iterable<EasyLauncherConfig>,
    ): List<EasyLauncherConfig> = ribbonProductFlavors.filter { config -> variant.productFlavors.any { config.name == it.second } } +
        ribbonBuildTypes.filter { it.name == variant.buildType }

    // Define priority order: variant-specific > flavor-specific > build-type-specific > main
    private fun resDirectoriesComparator(variant: Variant, projectPath: Directory) = compareBy<File> { dir ->
        val variantName = variant.name
        val flavorName = variant.flavorName
        val buildType = variant.buildType

        val path = dir.relativeToOrSelf(projectPath.asFile).path
        when {
            path.contains(variantName) -> 4
            flavorName?.isNotBlank() == true && path.contains(flavorName) -> 3
            buildType?.isNotBlank() == true && path.contains(buildType) -> 2
            path.contains("main") -> 1
            else -> 0
        }
    }
}

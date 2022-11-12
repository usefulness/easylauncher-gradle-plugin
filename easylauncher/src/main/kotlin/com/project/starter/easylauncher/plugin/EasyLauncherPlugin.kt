package com.project.starter.easylauncher.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet
import com.android.build.gradle.internal.api.DefaultAndroidSourceFile
import com.android.build.gradle.internal.scope.InternalArtifactType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import java.io.File

class EasyLauncherPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val extension = extensions.create(EasyLauncherExtension.NAME, EasyLauncherExtension::class.java)

        logger.info("Running gradle version: ${gradle.gradleVersion}")

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        val manifestBySourceSet = mutableMapOf<String, File>()
        val resSourceDirectoriesBySourceSet = mutableMapOf<String, Set<File>>()

        androidComponents.finalizeDsl { common ->
            common.sourceSets
                .mapNotNull { sourceSet ->
                    (sourceSet.manifest as? DefaultAndroidSourceFile)?.srcFile?.let {
                        Pair(sourceSet.name, it)
                    }
                }
                .forEach {
                    manifestBySourceSet[it.first] = it.second
                }

            common.sourceSets
                .map { sourceSet ->
                    val sourceDirs = (sourceSet.res as? DefaultAndroidSourceDirectorySet)?.srcDirs ?: emptySet()
                    Pair(sourceSet.name, sourceDirs)
                }
                .forEach {
                    resSourceDirectoriesBySourceSet[it.first] = it.second
                }
        }

        androidComponents.onVariants { variant ->
            val configs = extension.variants.filter { it.name == variant.name }.takeIf { it.isNotEmpty() }
                ?: findConfigs(variant, extension.productFlavors, extension.buildTypes)

            val enabled = configs.all { it.enabled.get() }

            if (enabled) {
                val filters = configs.flatMap { it.filters.get() }.toMutableSet()

                // set default ribbon
                if (filters.isEmpty() /*&& variant.buildType.isDebuggable*/) { // TODO: API does not seem to have any way to query the debug flag of the build type
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

                if (filters.isNotEmpty()) {
                    val customIconNames = provider {
                        val global = extension.iconNames.orNull.orEmpty()
                        val variantSpecific = configs.flatMap { config -> config.iconNames.orNull.orEmpty() }
                        (global + variantSpecific).toSet()
                    }

                    val relevantSourcesSets = setOfNotNull(
                        "main",
                        variant.name,
                        variant.buildType,
                        variant.flavorName,
                    )

                    val manifests = manifestBySourceSet
                        .mapNotNull { (name, file) ->
                            if (relevantSourcesSets.contains(name)) {
                                file
                            } else {
                                null
                            }
                        }

                    val resSourceDirectories = resSourceDirectoriesBySourceSet
                        .mapNotNull { (name, files) ->
                            if (relevantSourcesSets.contains(name)) {
                                files
                            } else {
                                null
                            }
                        }
                        .flatten()

                    val task = project.tasks.register("easylauncher${variant.name.capitalized()}", EasyLauncherTask::class.java) {
                        it.manifestFiles.set(manifests)
                        it.manifestPlaceholders.set(variant.manifestPlaceholders)
                        it.resourceDirectories.set(resSourceDirectories)
                        it.filters.set(filters)
                        it.customIconNames.set(customIconNames)
                        it.minSdkVersion.set(variant.minSdkVersion.apiLevel)
                    }

                    variant
                        .artifacts
                        .use(task)
                        .wiredWith(EasyLauncherTask::outputDir)
                        .toCreate(InternalArtifactType.GENERATED_RES)
                }
            } else {
                logger.info("disabled for ${variant.name}")
            }
        }
    }

    private fun findConfigs(
        variant: com.android.build.api.variant.Variant,
        ribbonProductFlavors: Iterable<EasyLauncherConfig>,
        ribbonBuildTypes: Iterable<EasyLauncherConfig>,
    ): List<EasyLauncherConfig> {
        return ribbonProductFlavors.filter { config -> variant.productFlavors.any { config.name == it.second } } +
            ribbonBuildTypes.filter { it.name == variant.buildType }
    }
}

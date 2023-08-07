package com.project.starter.easylauncher.plugin

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import com.project.starter.easylauncher.plugin.internal.replaceFirstChar
import com.project.starter.easylauncher.plugin.internal.titleCase
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

@Suppress("UnstableApiUsage")
class EasyLauncherPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val extension = extensions.create(EasyLauncherExtension.NAME, EasyLauncherExtension::class.java)

        val androidComponents = project.extensions.findByType(AndroidComponentsExtension::class.java)
            ?: error("'com.starter.easylauncher' has to be applied after Android Gradle Plugin")
        val agpVersion = androidComponents.pluginVersion
        log.info { "Environment: gradle=${gradle.gradleVersion}, agp=$agpVersion" }
        afterEvaluate {
            if (extension.showWarnings.orNull == true) {
                if (agpVersion.hasBrokenResourcesMerging && !agpVersion.canUseNewResources) {
                    log.warn {
                        "Plugin runs in compatibility mode, it will replace all resValues. " +
                            "Visit https://github.com/usefulness/easylauncher-gradle-plugin/issues/382 for more details."
                    }
                }
            }
        }

        val manifestBySourceSet = mutableMapOf<String, File>()
        val resSourceDirectoriesBySourceSet = mutableMapOf<String, Set<File>>()

        androidComponents.finalizeDsl { common ->
            common.sourceSets
                .mapNotNull { sourceSet -> sourceSet.manifest.srcFile?.let { sourceSet.name to it } }
                .forEach { manifestBySourceSet[it.first] = it.second }

            common.sourceSets
                .map { sourceSet -> sourceSet.name to sourceSet.res.srcDirs }
                .forEach { resSourceDirectoriesBySourceSet[it.first] = it.second }
        }

        androidComponents.onVariants { variant ->
            val configs = extension.variants.filter { it.name == variant.name }.takeIf { it.isNotEmpty() }
                ?: findConfigs(variant, extension.productFlavors, extension.buildTypes)

            val enabled = configs.all { it.enabled.get() }

            if (enabled) {
                val filters = configs.flatMap { it.filters.get() }.toMutableSet()

                // set default ribbon
                if (filters.isEmpty() && variant.isDebuggable) {
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

                log.info { "configuring ${variant.name}, isDebuggable=${variant.isDebuggable}, filters=${filters.size}" }

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
                    ) + variant.productFlavors.map { (_, flavor) ->
                        flavor
                    }

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

                    val capitalisedVariantName = variant.name.replaceFirstChar(Char::titleCase)
                    val task = project.tasks.register("easylauncher$capitalisedVariantName", EasyLauncherTask::class.java) {
                        it.manifestFiles.set(manifests)
                        it.manifestPlaceholders.set(variant.manifestPlaceholders)
                        it.resourceDirectories.set(resSourceDirectories)
                        it.filters.set(filters)
                        it.customIconNames.set(customIconNames)
                        it.minSdkVersion.set(variant.minSdkCompat(agpVersion))
                    }

                    if (agpVersion.canUseNewResources) {
                        // proper solution, unavailable in 7.3. https://issuetracker.google.com/issues/237303854
                        variant.sources.res?.addGeneratedSourceDirectory(task, EasyLauncherTask::outputDir)
                    } else if (agpVersion.hasBrokenResourcesMerging) {
                        // has side-effects, but "works". @see: https://github.com/usefulness/easylauncher-gradle-plugin/issues/382
                        variant
                            .artifacts
                            .use(task)
                            .wiredWith(EasyLauncherTask::outputDir)
                            .toCreate(com.android.build.gradle.internal.scope.InternalArtifactType.GENERATED_RES)
                    } else {
                        // legacy way to hook up the plugin
                        val generatedResDir = buildDir.resolve("generated/easylauncher/res/${variant.name}")
                        task.configure { it.outputDir.set(generatedResDir) }
                        project.afterEvaluate {
                            val android = extensions.getByName("android") as BaseExtension
                            android.sourceSets.getByName(variant.name).res.srcDir(generatedResDir)
                            tasks.named("generate${capitalisedVariantName}Resources") { it.dependsOn(task) }
                        }
                    }
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
    ): List<EasyLauncherConfig> {
        return ribbonProductFlavors.filter { config -> variant.productFlavors.any { config.name == it.second } } +
            ribbonBuildTypes.filter { it.name == variant.buildType }
    }

    private val AndroidPluginVersion.canUseNewResources get() = this >= AndroidPluginVersion(7, 4).beta(2)
    private val AndroidPluginVersion.hasBrokenResourcesMerging get() = this >= AndroidPluginVersion(7, 3).alpha(1)
}

@Suppress("DEPRECATION")
private fun Variant.minSdkCompat(agpVersion: AndroidPluginVersion) =
    if (agpVersion >= AndroidPluginVersion(8, 1)) minSdk.apiLevel else minSdkVersion.apiLevel

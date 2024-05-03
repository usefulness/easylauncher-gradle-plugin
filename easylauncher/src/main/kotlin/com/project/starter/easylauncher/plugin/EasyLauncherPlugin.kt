package com.project.starter.easylauncher.plugin

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

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

        lateinit var manifestBySourceSet: Map<String, File>
        lateinit var resSourceDirectoriesBySourceSet: Map<String, Set<File>>

        if (!agpVersion.canUseVariantManifestSources || !agpVersion.canAccessStaticVariantSources) {
            @Suppress("UnstableApiUsage")
            androidComponents.finalizeDsl { common ->
                if (!agpVersion.canUseVariantManifestSources) {
                    manifestBySourceSet = common.sourceSets
                        .mapNotNull { sourceSet -> sourceSet.manifest.srcFile?.let { sourceSet.name to it } }
                        .toMap()
                }

                if (!agpVersion.canAccessStaticVariantSources) {
                    resSourceDirectoriesBySourceSet = common.sourceSets
                        .map { sourceSet -> sourceSet.name to sourceSet.res.srcDirs }
                        .toMap()
                }
            }
        }

        androidComponents.onVariants { variant ->
            val configs = extension.variants.filter { it.name == variant.name }.takeIf { it.isNotEmpty() }
                ?: findConfigs(variant, extension.productFlavors, extension.buildTypes)

            val enabled = configs.all { it.enabled.get() }

            if (enabled) {
                val filters = configs.flatMap { it.filters.get() }.toMutableSet()

                val isDebuggable = if (agpVersion.hasDebuggableProperty) {
                    variant.debuggable
                } else {
                    variant.debuggableCompat
                }
                // set default ribbon
                if (filters.isEmpty() && isDebuggable) {
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

                log.info { "configuring ${variant.name}, isDebuggable=$isDebuggable, filters=${filters.size}" }

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

                    val manifests = if (agpVersion.canUseVariantManifestSources) {
                        variant.sources.manifests.all.map { manifests -> manifests.map { it.asFile } }
                    } else {
                        project.provider {
                            manifestBySourceSet
                                .mapNotNull { (name, file) ->
                                    if (relevantSourcesSets.contains(name)) {
                                        file
                                    } else {
                                        null
                                    }
                                }
                        }
                    }

                    val resSourceDirectories = if (agpVersion.canAccessStaticVariantSources) {
                        variant.sources.res?.static?.map { outer -> outer.flatten().map { inner -> inner.asFile } }
                            ?: project.provider { emptyList() }
                    } else {
                        project.provider {
                            resSourceDirectoriesBySourceSet
                                .mapNotNull { (name, files) ->
                                    if (relevantSourcesSets.contains(name)) {
                                        files
                                    } else {
                                        null
                                    }
                                }
                                .flatten()
                        }
                    }

                    val capitalisedVariantName = variant.name.replaceFirstChar(Char::titlecase)
                    val task = project.tasks.register("easylauncher$capitalisedVariantName", EasyLauncherTask::class.java) {
                        it.manifestFiles.set(manifests)
                        it.manifestPlaceholders.set(variant.manifestPlaceholders)
                        it.resourceDirectories.set(resSourceDirectories)
                        it.filters.set(filters)
                        it.customIconNames.set(customIconNames)
                        @Suppress("DEPRECATION")
                        it.minSdkVersion.set(if (agpVersion.canUseNewMinSdk) variant.minSdk.apiLevel else variant.minSdkVersion.apiLevel)
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
                        val generatedResDir = layout.buildDirectory.map { it.dir("generated/easylauncher/res/${variant.name}") }
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
    ): List<EasyLauncherConfig> = ribbonProductFlavors.filter { config -> variant.productFlavors.any { config.name == it.second } } +
        ribbonBuildTypes.filter { it.name == variant.buildType }

    private val AndroidPluginVersion.canUseVariantManifestSources get() = this >= AndroidPluginVersion(8, 3, 0)
    private val AndroidPluginVersion.hasDebuggableProperty get() = this >= AndroidPluginVersion(8, 3, 0)
    private val AndroidPluginVersion.canUseNewResources get() = this >= AndroidPluginVersion(7, 4).beta(2)
    private val AndroidPluginVersion.canAccessStaticVariantSources get() = this >= AndroidPluginVersion(8, 4).alpha(13)
    private val AndroidPluginVersion.hasBrokenResourcesMerging get() = this >= AndroidPluginVersion(7, 3).alpha(1)
    private val AndroidPluginVersion.canUseNewMinSdk get() = this >= AndroidPluginVersion(8, 1, 0)
}

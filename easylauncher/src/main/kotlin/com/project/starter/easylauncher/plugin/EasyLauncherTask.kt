package com.project.starter.easylauncher.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.project.starter.easylauncher.filter.EasyLauncherFilter
import com.project.starter.easylauncher.plugin.models.AdaptiveIcon
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.system.measureTimeMillis

open class EasyLauncherTask : DefaultTask() {

    @Input
    val variantName: Property<String> = property<String>()

    @OutputDirectory
    val outputDir: RegularFileProperty = project.objects.fileProperty()

    @Input
    val filters: ListProperty<EasyLauncherFilter> = listProperty<EasyLauncherFilter>()

    @Input
    val iconsNames: ListProperty<String> = listProperty<String>()

    @TaskAction
    fun run() {
        if (filters.get().isEmpty()) {
            return
        }

        val taskExecutionTime = measureTimeMillis {
            val android = project.extensions.getByType(AppExtension::class.java)
            val variant = android.applicationVariants.find { it.name == variantName.get() }
                ?: throw GradleException("invalid variant name ${variantName.get()}")

            val names = (iconsNames.orNull?.takeIf { it.isNotEmpty() } ?: android.getLauncherIconNames(variant)).toSet()
            logger.info("will process icons: ${names.joinToString()}")
            val icons = variant.getAllSourceSets().flatMap { resDir ->
                names.flatMap { resDir.getIconFiles(it) }
            }
            icons.forEach { iconFile ->
                val adaptiveIcon = iconFile.asAdaptiveIcon()
                if (adaptiveIcon == null) {
                    val outputFile = iconFile.getOutputFile()
                    iconFile.transformPng(outputFile, filters.get(), adaptive = false)
                } else {
                    variant.processIcon(adaptiveIcon)
                }
            }
        }

        logger.info("task finished in $taskExecutionTime ms")
    }

    private fun ApplicationVariant.getAllSourceSets() =
        sourceSets.flatMap { sourceSet -> sourceSet.resDirectories }
            .filterNot { resDirectory -> resDirectory == outputDir.asFile.get() }

    private fun ApplicationVariant.processIcon(adaptiveIcon: AdaptiveIcon) {
        getAllSourceSets().forEach { resDir ->
            val icons = resDir.getIconFiles(adaptiveIcon.foreground)
            icons.forEach { iconFile ->
                logger.info("found foreground at: ${project.relativePath(iconFile.path)}")
                val outputFile = iconFile.getOutputFile()
                if (iconFile.extension == "xml") {
                    iconFile.transformXml(outputFile, filters.get())
                } else {
                    iconFile.transformPng(outputFile, filters.get(), adaptive = true)
                }
            }
        }
    }

    private fun File.getIconFiles(iconName: String): Iterable<File> =
        project.fileTree(this) { it.include(resourceFilePattern(iconName)) }

    private fun resourceFilePattern(name: String): String {
        return if (name.startsWith("@")) {
            val (baseResType, fileName) = name.substring(1).split("/".toRegex(), 2)
            "$baseResType*/$fileName.*"
        } else {
            name
        }
    }

    private fun AppExtension.getLauncherIconNames(variant: ApplicationVariant) =
        getAndroidManifestFiles(variant)
            .mapNotNull { manifestFile -> manifestFile.getLauncherIcon() }

    private fun AppExtension.getAndroidManifestFiles(variant: ApplicationVariant): Iterable<File> {
        return listOf("main", variant.name, variant.buildType.name, variant.flavorName)
            .filter { it.isNotEmpty() }
            .distinct()
            .map { name -> project.file(sourceSets.getByName(name).manifest.srcFile) }
            .filter { it.exists() }
    }

    private inline fun <reified T> DefaultTask.listProperty(default: Iterable<T> = emptyList()) =
        project.objects.listProperty(T::class.java).apply {
            set(default)
        }

    private inline fun <reified T> DefaultTask.property(default: T? = null) =
        project.objects.property(T::class.java).apply {
            set(default)
        }

    private fun File.getOutputFile(): File =
        File(outputDir.asFile.get(), "${parentFile.name}/$name")

    companion object {
        const val NAME = "easylauncher"
    }
}

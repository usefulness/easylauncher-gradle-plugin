package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.filter.EasyLauncherFilter
import com.project.starter.easylauncher.plugin.models.AdaptiveIcon
import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@CacheableTask
abstract class EasyLauncherTask @Inject constructor(
    private val objects: ObjectFactory
) : DefaultTask() {

    @get:Input
    abstract val manifestFiles: ListProperty<File>

    @get:Input
    abstract val manifestPlaceholders: MapProperty<String, String>

    @get:Input
    abstract val resourceDirectories: SetProperty<File>

    @get:Input
    abstract val filters: ListProperty<EasyLauncherFilter>

    @get:Input
    abstract val customIconNames: SetProperty<String>

    @get:Input
    abstract val minSdkVersion: Property<Int>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun run() {
        val taskExecutionTime = measureTimeMillis {
            val iconNames = customIconNames.get().takeIf { it.isNotEmpty() } ?: getLauncherIconNames()
            val icons = getIcons(iconNames)

            icons.forEach { iconFile ->
                val adaptiveIcon = iconFile.asAdaptiveIcon()
                if (adaptiveIcon == null) {
                    val outputFile = iconFile.getOutputFile()
                    iconFile.transformImage(outputFile, filters.get(), adaptive = false)
                } else {
                    processIcon(adaptiveIcon)
                }
            }
        }

        logger.info("task finished in $taskExecutionTime ms")
    }

    private fun getLauncherIconNames(): Set<String> {
        return manifestFiles
            .get()
            .filter { it.exists() }
            .map {
                it.getLauncherIcons(manifestPlaceholders.get())
            }
            .flatten()
            .toSet()
    }

    private fun getIcons(iconNames: Set<String>): List<File> {
        logger.info("will process icons: ${iconNames.joinToString()}")

        return resourceDirectories
            .get()
            .filter {
                it.exists()
            }
            .flatMap { resDir ->
                iconNames.flatMap {
                    objects.getIconFiles(parent = resDir, iconName = it)
                }
            }
    }

    private fun processIcon(adaptiveIcon: AdaptiveIcon) {
        resourceDirectories
            .get()
            .forEach { resDir ->
                val icons = objects.getIconFiles(parent = resDir, iconName = adaptiveIcon.foreground)
                icons.forEach { iconFile ->
                    val outputFile = iconFile.getOutputFile()
                    if (iconFile.extension == "xml") {
                        iconFile.transformXml(outputFile, minSdkVersion.get(), filters.get())
                    } else {
                        iconFile.transformImage(outputFile, filters.get(), adaptive = true)
                    }
                }
            }
    }

    private fun File.getOutputFile(): File =
        File(outputDir.asFile.get(), "${parentFile.name}/$name")
}

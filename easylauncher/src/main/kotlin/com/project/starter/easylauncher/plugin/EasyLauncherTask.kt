package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.filter.EasyLauncherFilter
import com.project.starter.easylauncher.plugin.models.IconFile
import com.project.starter.easylauncher.plugin.models.IconType
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
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
abstract class EasyLauncherTask @Inject constructor(private val objects: ObjectFactory) : DefaultTask() {

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
            val customIcons = customIconNames.get().takeIf { it.isNotEmpty() }?.associateWith { IconType.Default }
            val icons = getIcons(customIcons ?: getLauncherIconNames())

            icons.forEach { iconFile ->
                when (iconFile) {
                    is IconFile.Raster -> iconFile.file.transformImage(
                        outputFile = iconFile.file.getOutputFile(),
                        filters = filters.get(),
                        modifier = null,
                    )

                    is IconFile.RasterRound -> iconFile.file.transformImage(
                        outputFile = iconFile.file.getOutputFile(),
                        filters = filters.get(),
                        modifier = EasyLauncherFilter.Modifier.Round,
                    )

                    is IconFile.Adaptive -> iconFile.processAdaptiveIcon()
                    is IconFile.XmlDrawableResource -> iconFile.processDrawable()
                }
            }
        }

        log.info { "task finished in $taskExecutionTime ms" }
    }

    private fun getLauncherIconNames() = manifestFiles.get()
        .filter { it.exists() }
        .flatMap { it.getLauncherIcons(manifestPlaceholders.get()).entries }
        .associate { (key, value) -> key to value }

    private fun getIcons(iconNames: Map<String, IconType>): List<IconFile> {
        log.info { "will process icons: ${iconNames.keys.joinToString()}" }

        return resourceDirectories.get()
            .filter { it.exists() }
            .flatMap { resDir ->
                iconNames.flatMap { (iconName, iconType) ->
                    objects.getIconFiles(parent = resDir, iconName = iconName)
                        .map { iconFile ->
                            iconFile.tryParseXmlFile() ?: when (iconType) {
                                IconType.Default -> IconFile.Raster(iconFile)
                                IconType.Round -> IconFile.RasterRound(iconFile)
                            }
                        }
                }
            }
    }

    private fun IconFile.Adaptive.processAdaptiveIcon() {
        resourceDirectories.get().forEach { resDir ->
            val foregroundFiles = objects.getIconFiles(parent = resDir, iconName = foreground)
            val monochromeFiles = monochrome?.let { objects.getIconFiles(parent = resDir, iconName = it) } ?: emptyList()
            val iconFiles = (foregroundFiles + monochromeFiles).toSet()

            iconFiles.forEach { iconFile ->
                val outputFile = iconFile.getOutputFile()
                if (iconFile.extension == "xml") {
                    iconFile.transformXml(outputFile, minSdkVersion.get(), filters.get())
                } else {
                    iconFile.transformImage(
                        outputFile = outputFile,
                        filters = filters.get(),
                        modifier = EasyLauncherFilter.Modifier.Adaptive,
                    )
                }
            }
        }
    }

    private fun IconFile.XmlDrawableResource.processDrawable() {
        val outputFile = file.getOutputFile()
        file.transformXml(outputFile, minSdkVersion.get(), filters.get())
    }

    private fun File.getOutputFile(): File = File(outputDir.asFile.get(), "${parentFile.name}/$name")
}

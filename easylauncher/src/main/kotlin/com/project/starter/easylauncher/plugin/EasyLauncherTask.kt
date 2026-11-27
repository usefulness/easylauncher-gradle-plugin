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

                    is IconFile.XmlDrawable -> iconFile.processDrawable()
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
                            iconFile.tryParseXmlIcon() ?: when (iconType) {
                                IconType.Default -> IconFile.Raster(iconFile)
                                IconType.Round -> IconFile.RasterRound(iconFile)
                            }
                        }
                }
            }
    }

    private fun IconFile.Adaptive.processAdaptiveIcon() {
        resourceDirectories.get().forEach { resDir ->
            objects.getIconFiles(parent = resDir, iconName = foreground).forEach { iconFile ->
                iconFile.transformLayer(iconFile.getOutputFile(), EasyLauncherFilter.Modifier.Adaptive)
            }
        }
        val transformedMonochrome = monochrome?.takeIf { name ->
            resourceDirectories.get()
                .flatMap { resDir -> objects.getIconFiles(parent = resDir, iconName = name) }
                .map { iconFile ->
                    iconFile.transformLayer(iconFile.getOutputFile(MONOCHROME_SUFFIX), EasyLauncherFilter.Modifier.Monochrome)
                }
                .any { it }
        }

        val adaptiveIconXml = file.readText()
        val outputXml = transformedMonochrome
            ?.let { adaptiveIconXml.replaceMonochromeDrawable(from = it, to = "$it$MONOCHROME_SUFFIX") }
            ?: adaptiveIconXml
        file.getOutputFile().apply { parentFile.mkdirs() }.writeText(outputXml)
    }

    private fun File.transformLayer(outputFile: File, modifier: EasyLauncherFilter.Modifier): Boolean = if (extension == "xml") {
        when (val drawable = tryParseXmlDrawable()) {
            is IconFile.XmlDrawable.Vector -> {
                drawable.transform(outputFile, minSdkVersion.get(), filters.get(), modifier)
                true
            }

            null -> {
                log.info { "Skipped $this due to unrecognised file format" }
                false
            }
        }
    } else {
        transformImage(outputFile = outputFile, filters = filters.get(), modifier = modifier)
        true
    }

    private fun IconFile.XmlDrawable.processDrawable() {
        when (this) {
            is IconFile.XmlDrawable.Vector -> transform(file.getOutputFile(), minSdkVersion.get(), filters.get())
        }
    }

    private fun File.getOutputFile(suffix: String = ""): File =
        File(outputDir.asFile.get(), "${parentFile.name}/$nameWithoutExtension$suffix.$extension")
}

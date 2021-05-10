package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.filter.EasyLauncherFilter
import com.project.starter.easylauncher.plugin.models.AdaptiveIcon
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@CacheableTask
open class EasyLauncherTask @Inject constructor(
    private val objectFactory: ObjectFactory,
) : DefaultTask() {

    @OutputDirectory
    val outputDir: RegularFileProperty = objectFactory.fileProperty()

    @Input
    val filters: ListProperty<EasyLauncherFilter> = listProperty<EasyLauncherFilter>()

    @Input
    val minSdkVersion: Property<Int> = property(default = null)

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    val resourceDirectories: ConfigurableFileCollection = objectFactory.fileCollection()

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    val icons: ConfigurableFileCollection = objectFactory.fileCollection()

    @TaskAction
    fun run() {
        if (filters.get().isEmpty()) {
            return
        }

        val taskExecutionTime = measureTimeMillis {
            icons.forEach { iconFile ->
                val adaptiveIcon = iconFile.asAdaptiveIcon()
                if (adaptiveIcon == null) {
                    val outputFile = iconFile.getOutputFile()
                    iconFile.transformPng(outputFile, filters.get(), adaptive = false)
                } else {
                    processIcon(adaptiveIcon, minSdkVersion.get())
                }
            }
        }

        logger.info("task finished in $taskExecutionTime ms")
    }

    private fun processIcon(adaptiveIcon: AdaptiveIcon, minSdkVersion: Int) {
        resourceDirectories.forEach { resDir ->
            val icons = objectFactory.getIconFiles(parent = resDir, iconName = adaptiveIcon.foreground)
            icons.forEach { iconFile ->
                val outputFile = iconFile.getOutputFile()
                if (iconFile.extension == "xml") {
                    iconFile.transformXml(outputFile, minSdkVersion, filters.get())
                } else {
                    iconFile.transformPng(outputFile, filters.get(), adaptive = true)
                }
            }
        }
    }

    private inline fun <reified T> listProperty(default: Iterable<T> = emptyList()) =
        objectFactory.listProperty(T::class.java).apply {
            set(default)
        }

    private inline fun <reified T> property(default: T? = null) =
        objectFactory.property(T::class.java).apply {
            set(default)
        }

    private fun File.getOutputFile(): File =
        File(outputDir.asFile.get(), "${parentFile.name}/$name")

    companion object {
        const val NAME = "easylauncher"
    }
}

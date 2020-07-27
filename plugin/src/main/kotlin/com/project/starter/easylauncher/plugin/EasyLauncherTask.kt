package com.project.starter.easylauncher.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.project.starter.easylauncher.filter.EasyLauncherFilter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class EasyLauncherTask : DefaultTask() {

    companion object {
        const val NAME = "easylauncher"
    }

    @Internal
    lateinit var variant: ApplicationVariant

    @OutputDirectory
    val outputDir = project.objects.fileProperty()

    // `iconNames` includes: "@drawable/icon", "@mipmap/ic_launcher", etc.
    @Input
    val iconNames = listProperty<String>()

    @Input
    val foregroundIconNames = listProperty<String>()

    @Input
    val filters = listProperty<EasyLauncherFilter>()

    @TaskAction
    fun run() {
        if (filters.get().isEmpty()) {
            return
        }

        val startTime = System.currentTimeMillis()

        val names = (iconNames.get() + getLauncherIconNames()).toSet()
        val foregroundNames = foregroundIconNames.get().toSet()
        variant.sourceSets
            .flatMap { it.resDirectories }
            .forEach { resDir ->
                if (resDir == outputDir) {
                    return
                }

                names.forEach { name ->
                    project.fileTree(resDir) {
                        it.include(Resources.resourceFilePattern(name))
                        it.exclude("**/*.xml")
                    }.forEach { inputFile ->
                        logger.info("process $inputFile")

                        val basename = inputFile.name
                        val resType = inputFile.parentFile.name
                        val outputFile = File(outputDir.asFile.get(), "$resType/$basename")
                        outputFile.parentFile.mkdirs()

                        val easyLauncher = EasyLauncher(inputFile, outputFile)
                        easyLauncher.process(filters.get())
                        easyLauncher.save()
                    }
                }
                foregroundNames.forEach { name ->
                    project.fileTree(resDir) {
                        it.include(Resources.resourceFilePattern(name))
                        it.exclude("**/*.xml")
                    }
                        .forEach { inputFile ->
                            logger.info("process $inputFile")

                            val basename = inputFile.name
                            val resType = inputFile.parentFile.name
                            val outputFile = File(outputDir.asFile.get(), "$resType/$basename")
                            outputFile.parentFile.mkdirs()

                            val largeRibbonFilters = filters.get()
                                .onEach { it.setAdaptiveLauncherMode(true) }

                            val easyLauncher = EasyLauncher(inputFile, outputFile)
                            easyLauncher.process(largeRibbonFilters)
                            easyLauncher.save()
                        }
                }
            }

        logger.info("task finished in ${System.currentTimeMillis() - startTime}ms")
    }

    private fun getLauncherIconNames() =
        getAndroidManifestFiles().flatMap { manifestFile -> Resources.getLauncherIcons(manifestFile) }

    private fun getAndroidManifestFiles(): Iterable<File> {
        val android = project.extensions.getByType(AppExtension::class.java)

        return listOf("main", variant.name, variant.buildType.name, variant.flavorName)
            .filter { it.isNotEmpty() }
            .distinct()
            .map { name -> project.file(android.sourceSets.getByName(name).manifest.srcFile) }
            .filter { it.exists() }
    }

    internal inline fun <reified T> DefaultTask.listProperty(default: Iterable<T> = emptyList()) =
        project.objects.listProperty(T::class.java).apply {
            set(default)
        }
}

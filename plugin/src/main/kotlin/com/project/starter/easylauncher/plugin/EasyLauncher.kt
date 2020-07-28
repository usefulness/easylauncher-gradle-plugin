package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.filter.EasyLauncherFilter
import java.io.File
import javax.imageio.ImageIO

class EasyLauncher(inputFile: File, private val outputFile: File) {

    private val image = ImageIO.read(inputFile)

    fun save() {
        outputFile.parentFile.mkdirs()
        ImageIO.write(image, "png", outputFile)
    }

    fun process(filters: Iterable<EasyLauncherFilter>) {
        filters.forEach { it.apply(image) }
    }
}

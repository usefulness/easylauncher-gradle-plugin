package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.filter.EasyLauncherFilter
import com.project.starter.easylauncher.plugin.models.Size
import com.project.starter.easylauncher.plugin.models.toSize
import groovy.util.XmlSlurper
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt

internal fun File.transformPng(outputFile: File, filters: List<EasyLauncherFilter>, adaptive: Boolean) {
    val image = ImageIO.read(this)
    filters.forEach { it.apply(image, adaptive = adaptive) }
    outputFile.parentFile.mkdirs()
    ImageIO.write(image, "png", outputFile)
}

internal fun File.transformXml(outputFile: File, filters: List<EasyLauncherFilter>) {
    val iconXml = XmlSlurper().parse(this)
    val width = iconXml.property("@android:width")?.toSize().let(::requireNotNull)
    val height = iconXml.property("@android:height")?.toSize().let(::requireNotNull)

    outputFile.parentFile.mkdirs()

    val layers = filters.mapIndexed { index, filter ->
        val overlay = BufferedImage(width.value, height.value, BufferedImage.TYPE_INT_ARGB)
        filter.apply(overlay, adaptive = true)

        val filterId = "${filter::class.java.simpleName.toLowerCase()}_$index"
        val overlayFile = outputFile.parentFile.resolve("${filterId}_${outputFile.nameWithoutExtension}.png")
        ImageIO.write(overlay, "png", overlayFile)
        overlayFile.nameWithoutExtension
    }
        .joinToString(separator = "\n") {
            """
            |   <item
            |       android:width="${width.androidSize}"
            |       android:height="${height.androidSize}"
            |       android:drawable="@${outputFile.parentFile.name}/$it"
            |       android:gravity="center"
            |       />
            |""".trimMargin()
        }
    copyTo(outputFile.parentFile.resolve("easy_$name"), overwrite = true)

    outputFile.writeText(
        """
        |<?xml version="1.0" encoding="utf-8"?>
        |<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
        |
        |   <item android:drawable="@drawable/easy_$nameWithoutExtension" />
        |   
        |$layers
        |</layer-list>
        |""".trimMargin()
    )
}

private val Size.androidSize: String
    get() = "${(value * FOREGROUND_LAYER_MULTIPLIER).roundToInt()}$unit"

private const val FOREGROUND_LAYER_MULTIPLIER = 0.75

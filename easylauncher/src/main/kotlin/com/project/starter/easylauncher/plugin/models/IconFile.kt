package com.project.starter.easylauncher.plugin.models

import java.io.File

internal sealed class IconFile {

    data class Raster(val file: File) : IconFile()

    data class RasterRound(val file: File) : IconFile()

    data class Adaptive(
        val file: File,
        val background: String,
        val foreground: String,
        val monochrome: String?,
    ) : IconFile()

    sealed class XmlDrawable : IconFile() {

        data class Vector(
            val file: File,
            val width: Int,
            val height: Int,
        ) : XmlDrawable()
    }
}

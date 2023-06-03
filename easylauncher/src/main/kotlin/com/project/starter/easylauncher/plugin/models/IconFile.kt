package com.project.starter.easylauncher.plugin.models

import java.io.File

internal sealed class IconFile {

    data class Raster(val file: File) : IconFile()

    data class RasterRound(val file: File) : IconFile()

    data class Adaptive(val file: File, val background: String, val foreground: String) : IconFile()

    data class XmlDrawableResource(val file: File) : IconFile()
}

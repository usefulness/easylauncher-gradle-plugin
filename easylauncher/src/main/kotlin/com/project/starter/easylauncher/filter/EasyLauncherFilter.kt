package com.project.starter.easylauncher.filter

import java.awt.image.BufferedImage
import java.io.Serializable

interface EasyLauncherFilter : Serializable {

    fun apply(image: BufferedImage, adaptive: Boolean = false)
}

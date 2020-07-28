package com.project.starter.easylauncher.filter

import java.awt.image.BufferedImage
import java.io.Serializable

interface EasyLauncherFilter : Serializable {

    fun setAdaptiveLauncherMode(enable: Boolean)

    fun apply(image: BufferedImage)
}

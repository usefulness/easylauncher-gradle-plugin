package com.project.starter.easylauncher.filter

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

@Suppress("MagicNumber")
class OverlayFilter(private val fgFile: File) : EasyLauncherFilter {

    override fun apply(image: BufferedImage, adaptive: Boolean) {
        var fgImage: Image? = null
        try {
            fgImage = ImageIO.read(fgFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (fgImage != null) {
            val width = image.width.toFloat()
            val height = image.width.toFloat()
            var scale =
                (width / fgImage.getWidth(null).toFloat()).coerceAtMost(height / fgImage.getHeight(null))
            if (adaptive) {
                scale *= (72f / 108)
            }
            val fgImageScaled = fgImage.getScaledInstance(
                (fgImage.getWidth(null) * scale).toInt(),
                (fgImage.getWidth(null) * scale).toInt(),
                Image.SCALE_SMOOTH
            )
            val graphics = image.createGraphics()

            // TODO allow to choose the gravity for the overlay
            // TODO allow to choose the scaling type
            if (adaptive) {
                graphics.drawImage(fgImageScaled, (width * (1 - 72f / 108) / 2).toInt(), (height * (1 - 72f / 108) / 2).toInt(), null)
            } else {
                graphics.drawImage(fgImageScaled, 0, 0, null)
            }
            graphics.dispose()
        }
    }
}

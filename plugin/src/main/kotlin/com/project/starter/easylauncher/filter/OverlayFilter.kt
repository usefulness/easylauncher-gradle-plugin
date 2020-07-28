package com.project.starter.easylauncher.filter

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

@Suppress("MagicNumber")
class OverlayFilter(private val fgFile: File) : EasyLauncherFilter {

    private var addPadding = false

    override fun setAdaptiveLauncherMode(enable: Boolean) {
        addPadding = enable
    }

    override fun apply(image: BufferedImage) {
        var fgImage: Image? = null
        try {
            fgImage = ImageIO.read(fgFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (fgImage != null) {
            val width = image.width
            val height = image.width
            var scale =
                Math.min(width / fgImage.getWidth(null).toFloat(), height / fgImage.getHeight(null).toFloat())
            if (addPadding) {
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
            if (addPadding) {
                graphics.drawImage(fgImageScaled, (width * (1 - 72f / 108) / 2).toInt(), (height * (1 - 72f / 108) / 2).toInt(), null)
            } else {
                graphics.drawImage(fgImageScaled, 0, 0, null)
            }
            graphics.dispose()
        }
    }
}

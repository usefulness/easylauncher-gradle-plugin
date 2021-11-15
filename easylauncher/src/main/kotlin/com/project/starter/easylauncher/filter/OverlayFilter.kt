package com.project.starter.easylauncher.filter

import org.slf4j.LoggerFactory
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

@Suppress("MagicNumber")
class OverlayFilter(private val fgFile: File) : EasyLauncherFilter {

    private val logger
        get() = LoggerFactory.getLogger(this::class.java)

    override fun apply(image: BufferedImage, adaptive: Boolean) {
        val fgImage = try {
            ImageIO.read(fgFile)
        } catch (e: IOException) {
            logger.error("Failed to load overlay ${fgFile.name}", e)
            return
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
                Image.SCALE_SMOOTH,
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

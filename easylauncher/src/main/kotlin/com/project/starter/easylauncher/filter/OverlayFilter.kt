package com.project.starter.easylauncher.filter

import org.slf4j.LoggerFactory
import java.awt.Image
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.roundToInt

class OverlayFilter(private val fgFile: File) : EasyLauncherFilter {

    private val logger
        get() = LoggerFactory.getLogger(this::class.java)

    override fun apply(canvas: Canvas, modifier: EasyLauncherFilter.Modifier?) {
        try {
            val fgImage = ImageIO.read(fgFile)
            val fgWidth = fgImage.getWidth(null).toFloat()
            val fgHeight = fgImage.getHeight(null).toFloat()

            val scale = if (modifier == EasyLauncherFilter.Modifier.Adaptive) ADAPTIVE_SCALE else 1f
            val imageScale = scale * (canvas.width / fgWidth).coerceAtMost(canvas.height / fgHeight)
            val scaledWidth = (fgWidth * imageScale).roundToInt()
            val scaledHeight = (fgHeight * imageScale).roundToInt()
            val fgImageScaled = fgImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)

            canvas.use { graphics ->
                // TODO allow to choose the gravity for the overlay
                // TODO allow to choose the scaling type
                graphics.drawImage(
                    fgImageScaled,
                    ((canvas.width - scaledWidth) / 2f).roundToInt(),
                    ((canvas.height - scaledHeight) / 2f).roundToInt(),
                    null,
                )
            }
        } catch (e: IOException) {
            logger.error("Failed to load overlay '${fgFile.absolutePath}'.", e)
            return
        }
    }

    companion object {

        private const val serialVersionUID: Long = 1
    }
}

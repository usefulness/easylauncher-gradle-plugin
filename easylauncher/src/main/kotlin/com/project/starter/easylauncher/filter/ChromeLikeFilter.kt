package com.project.starter.easylauncher.filter

import com.project.starter.easylauncher.plugin.ADAPTIVE_CONTENT_SCALE
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.roundToInt

class ChromeLikeFilter(
    private val label: String,
    ribbonColor: Color? = null,
    labelColor: Color? = null,
    fontName: String? = null,
    fontResource: File? = null,
) : EasyLauncherFilter {

    private val ribbonColor = ribbonColor ?: Color.DARK_GRAY
    private val labelColor = labelColor ?: Color.WHITE
    private val font = fontResource?.takeIf { it.exists() }
        ?.let { Font.createFont(Font.TRUETYPE_FONT, it) }
        ?: Font(fontName, Font.PLAIN, 1)

    override fun apply(image: BufferedImage, adaptive: Boolean) {
        val graphics = image.graphics as Graphics2D

        val frc = FontRenderContext(graphics.transform, true, true)
        // calculate the rectangle where the label is rendered
        val backgroundHeight = (image.height * OVERLAY_HEIGHT).roundToInt()
        graphics.font = getFont(
            imageHeight = image.height,
            maxLabelWidth = (image.width * ADAPTIVE_CONTENT_SCALE).roundToInt(),
            maxLabelHeight = (backgroundHeight * ADAPTIVE_CONTENT_SCALE).roundToInt(),
            frc = frc
        )
        val textBounds = graphics.font.getStringBounds(label, frc)

        // update y gravity after calculating font size
        val yGravity = image.height - backgroundHeight

        // draw the ribbon
        graphics.color = ribbonColor
        if (!adaptive) {
            graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1f)
        }
        graphics.fillRect(0, yGravity, image.width, backgroundHeight)

        // draw the label
        graphics.setPaintMode()
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.color = labelColor
        val fm = graphics.fontMetrics
        graphics.drawString(
            label,
            image.width / 2 - textBounds.width.toInt() / 2,
            yGravity + fm.ascent
        )
        graphics.dispose()
    }

    private fun getFont(imageHeight: Int, maxLabelWidth: Int, maxLabelHeight: Int, frc: FontRenderContext) =
        (imageHeight downTo 0).asSequence()
            .map { size -> font.deriveFont(size.toFloat()) }
            .first { font ->
                val bounds = font.getStringBounds(label, frc)
                bounds.width < maxLabelWidth && bounds.height < maxLabelHeight
            }

    companion object {
        private const val OVERLAY_HEIGHT = 0.4
    }
}

package com.project.starter.easylauncher.filter

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

@Suppress("MagicNumber")
class ChromeLikeFilter(
    private val label: String,
    ribbonColor: Color? = null,
    labelColor: Color? = null,
) : EasyLauncherFilter {

    private val ribbonColor = ribbonColor ?: Color.DARK_GRAY
    private val labelColor = labelColor ?: Color.WHITE

    private val fontName = "DEFAULT"
    private val fontStyle = Font.PLAIN

    override fun apply(image: BufferedImage, adaptive: Boolean) {
        val graphics = image.graphics as Graphics2D

        val frc = FontRenderContext(graphics.transform, true, true)
        // calculate the rectangle where the label is rendered
        val maxLabelWidth = (image.height * 0.4).roundToInt()
        graphics.font = getFont(image.height, maxLabelWidth, frc)
        val textBounds = graphics.font.getStringBounds(label, frc)

        // update y gravity after calculating font size
        val yGravity = image.height - maxLabelWidth

        // draw the ribbon
        graphics.color = ribbonColor
        graphics.fillRect(0, yGravity, image.width, maxLabelWidth)
        // draw the label
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

    private fun getFont(imageHeight: Int, maxLabelWidth: Int, frc: FontRenderContext): Font {
        var max = imageHeight / 4
        var min = 0

        // Automatic calculation: as big as possible
        var size = max
        for (i in 0..9) {
            val mid = (max + min) / 2
            if (mid == size) {
                break
            }
            val font = Font(fontName, fontStyle, mid)
            val labelBounds = font.getStringBounds(label, frc)
            if (labelBounds.width > maxLabelWidth * 0.67) {
                max = mid
            } else {
                min = mid
            }
            size = mid
        }
        return Font(fontName, fontStyle, size)
    }
}

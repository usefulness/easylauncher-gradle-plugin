package com.project.starter.easylauncher.filter

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Suppress("MagicNumber", "LongParameterList")
class ColorRibbonFilter(
    private val label: String,
    ribbonColor: Color? = null,
    labelColor: Color? = null,
    gravity: Gravity? = null,
    private val textSizeRatio: Float? = null,
    fontName: String? = null,
    fontResource: File? = null,
) : EasyLauncherFilter {

    enum class Gravity {
        TOP, BOTTOM, TOPLEFT, TOPRIGHT
    }

    private val ribbonColor = ribbonColor ?: Color(0, 0x72, 0, 0x99)
    private val labelColor = labelColor ?: Color.WHITE
    private val gravity = gravity ?: Gravity.TOPLEFT
    private val font = fontResource?.takeIf { it.exists() }
        ?.let { Font.createFont(Font.TRUETYPE_FONT, it) }
        ?: Font(fontName, Font.PLAIN, 1)

    @Suppress("ComplexMethod")
    override fun apply(image: BufferedImage, adaptive: Boolean) {
        val graphics = image.graphics as Graphics2D
        when (gravity) {
            Gravity.TOP, Gravity.BOTTOM -> Unit
            Gravity.TOPRIGHT -> graphics.transform = AffineTransform.getRotateInstance(
                Math.toRadians(45.0),
                image.width.toDouble(),
                0.0
            )
            Gravity.TOPLEFT -> graphics.transform = AffineTransform.getRotateInstance(Math.toRadians(-45.0))
        }
        val frc = FontRenderContext(graphics.transform, true, true)
        // calculate the rectangle where the label is rendered
        val maxLabelWidth = calculateMaxLabelWidth(image.height / 2)
        graphics.font = getFont(image.height, maxLabelWidth, frc)
        val textBounds = graphics.font.getStringBounds(label, frc)
        val textHeight = textBounds.height.toInt()
        val textPadding = textHeight / 10
        val labelHeight = textHeight + textPadding * 2

        // update y gravity after calculating font size
        val yGravity = when (gravity) {
            Gravity.TOP -> if (adaptive) image.height / 4 else 0
            Gravity.BOTTOM -> image.height - labelHeight - (if (adaptive) image.height / 4 else 0)
            Gravity.TOPRIGHT, Gravity.TOPLEFT -> image.height / (if (adaptive) 2 else 4)
        }

        // draw the ribbon
        graphics.color = ribbonColor
        if (gravity == Gravity.TOP || gravity == Gravity.BOTTOM) {
            graphics.fillRect(0, yGravity, image.width, labelHeight)
        } else if (gravity == Gravity.TOPRIGHT) {
            graphics.fillRect(0, yGravity, image.width * 2, labelHeight)
        } else {
            graphics.fillRect(-image.width, yGravity, image.width * 2, labelHeight)
        }
        // draw the label
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.color = labelColor
        val fm = graphics.fontMetrics
        if (gravity == Gravity.TOP || gravity == Gravity.BOTTOM) {
            graphics.drawString(
                label,
                image.width / 2 - textBounds.width.toInt() / 2,
                yGravity + fm.ascent
            )
        } else if (gravity == Gravity.TOPRIGHT) {
            graphics.drawString(
                label,
                image.width - textBounds.width.toInt() / 2,
                yGravity + fm.ascent
            )
        } else {
            graphics.drawString(
                label,
                (-textBounds.width).toInt() / 2,
                yGravity + fm.ascent
            )
        }
        graphics.dispose()
    }

    private fun getFont(imageHeight: Int, maxLabelWidth: Int, frc: FontRenderContext): Font {
        // User-defined text size
        if (textSizeRatio != null) {
            return font.deriveFont((imageHeight * textSizeRatio).roundToInt().toFloat())
        }
        val max = imageHeight / 8 - 1

        return (max downTo 0).asSequence()
            .map { size -> font.deriveFont(size.toFloat()) }
            .first { font ->
                val bounds = font.getStringBounds(label, frc)
                bounds.width < maxLabelWidth
            }
    }

    companion object {
        private fun calculateMaxLabelWidth(y: Int) =
            (y * sqrt(2.0)).roundToInt()
    }
}

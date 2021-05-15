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
    private val labelPadding: Int? = null,
    overlayHeight: Float? = null,
    gravity: Gravity?,
    private val textSizeRatio: Float? = null,
    fontName: String? = null,
    fontResource: File? = null,
) : EasyLauncherFilter {

    enum class Gravity {
        TOP, BOTTOM
    }

    private val ribbonColor = ribbonColor ?: Color.DARK_GRAY
    private val labelColor = labelColor ?: Color.WHITE
    private val font = fontResource?.takeIf { it.exists() }
        ?.let { Font.createFont(Font.TRUETYPE_FONT, it) }
        ?: fontName?.let { Font(it, Font.PLAIN, 1) }
        ?: DEFAULT_EASYLAUNCHER_FONT
    private val overlayHeight = overlayHeight ?: OVERLAY_HEIGHT
    private val gravity = gravity ?: Gravity.BOTTOM

    override fun apply(image: BufferedImage, adaptive: Boolean) {
        val graphics = image.graphics as Graphics2D

        val frc = FontRenderContext(graphics.transform, true, true)
        // calculate the rectangle where the label is rendered
        val backgroundHeight = (image.height * overlayHeight).roundToInt()
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
            graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f)
        }
        when (gravity) {
            Gravity.TOP -> graphics.fillRect(0, 0, image.width, backgroundHeight)
            Gravity.BOTTOM -> graphics.fillRect(0, yGravity, image.width, backgroundHeight)
        }

        // draw the label
        graphics.setPaintMode()
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.color = labelColor
        val fm = graphics.fontMetrics
        when (gravity) {
            Gravity.TOP ->
                graphics.drawString(
                    label,
                    image.width / 2 - textBounds.width.toInt() / 2,
                    backgroundHeight - fm.descent - (labelPadding ?: 0)
                )
            Gravity.BOTTOM ->
                graphics.drawString(
                    label,
                    image.width / 2 - textBounds.width.toInt() / 2,
                    yGravity + fm.ascent + (labelPadding ?: 0)
                )
        }
        graphics.dispose()
    }

    private fun getFont(imageHeight: Int, maxLabelWidth: Int, maxLabelHeight: Int, frc: FontRenderContext): Font {
        if (textSizeRatio != null) {
            return font.deriveFont((imageHeight * textSizeRatio).roundToInt().toFloat())
        }

        return (imageHeight downTo 0).asSequence()
            .map { size -> font.deriveFont(size.toFloat()) }
            .first { font ->
                val bounds = font.getStringBounds(label, frc)
                bounds.width < maxLabelWidth && bounds.height < maxLabelHeight
            }
    }

    companion object {
        private const val OVERLAY_HEIGHT = 0.4f
    }
}

package com.project.starter.easylauncher.filter

import com.project.starter.easylauncher.plugin.ADAPTIVE_CONTENT_SCALE
import com.project.starter.easylauncher.plugin.toColor
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.io.File
import kotlin.math.roundToInt

class ChromeLikeFilter(
    private val label: String,
    private val ribbonColor: String? = null,
    private val labelColor: String? = null,
    private val labelPadding: Int? = null,
    private val overlayHeight: Float? = null,
    private val gravity: Gravity? = null,
    private val textSizeRatio: Float? = null,
    private val fontName: String? = null,
    private val fontResource: File? = null,
) : EasyLauncherFilter {

    enum class Gravity {
        TOP, BOTTOM
    }

    private val _ribbonColor get() = ribbonColor?.toColor() ?: Color.DARK_GRAY
    private val _labelColor get() = labelColor?.toColor() ?: Color.WHITE
    private val _overlayHeight get() = overlayHeight ?: OVERLAY_HEIGHT
    private val _gravity get() = gravity ?: Gravity.BOTTOM

    override fun apply(canvas: Canvas, adaptive: Boolean) {
        canvas.use { graphics ->
            apply(canvas, graphics, adaptive)
        }
    }

    private fun apply(canvas: Canvas, graphics: Graphics2D, adaptive: Boolean) {
        val frc = FontRenderContext(graphics.transform, true, true)
        // calculate the rectangle where the label is rendered
        val backgroundHeight = (canvas.height * _overlayHeight).roundToInt()
        graphics.font = getFont(
            imageHeight = canvas.height,
            maxLabelWidth = (canvas.width * ADAPTIVE_CONTENT_SCALE).roundToInt(),
            maxLabelHeight = (backgroundHeight * ADAPTIVE_CONTENT_SCALE).roundToInt(),
            frc = frc,
        )
        val textBounds = graphics.font.getStringBounds(label, frc)

        // draw the ribbon
        graphics.color = _ribbonColor
        if (!adaptive) {
            graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f)
        }
        when (_gravity) {
            Gravity.TOP -> graphics.fillRect(
                -canvas.paddingLeft,
                -canvas.paddingTop,
                canvas.fullWidth,
                canvas.paddingTop + backgroundHeight,
            )
            Gravity.BOTTOM -> graphics.fillRect(
                -canvas.paddingLeft,
                canvas.height - backgroundHeight,
                canvas.fullWidth,
                canvas.paddingBottom + backgroundHeight,
            )
        }

        // draw the label
        graphics.setPaintMode()
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.color = _labelColor
        val fm = graphics.fontMetrics
        when (_gravity) {
            Gravity.TOP ->
                graphics.drawString(
                    label,
                    canvas.width / 2 - textBounds.width.toInt() / 2,
                    backgroundHeight - fm.descent - (labelPadding ?: 0),
                )
            Gravity.BOTTOM ->
                graphics.drawString(
                    label,
                    canvas.width / 2 - textBounds.width.toInt() / 2,
                    canvas.height - backgroundHeight + fm.ascent + (labelPadding ?: 0),
                )
        }
    }

    private fun getFont(imageHeight: Int, maxLabelWidth: Int, maxLabelHeight: Int, frc: FontRenderContext): Font {
        val fontFile = findFontFile(
            resource = fontResource,
            name = fontName,
        )
        if (textSizeRatio != null) {
            return fontFile.deriveFont((imageHeight * textSizeRatio).roundToInt().toFloat())
        }

        return (imageHeight downTo 0).asSequence()
            .map { size -> fontFile.deriveFont(size.toFloat()) }
            .first { font ->
                val bounds = font.getStringBounds(label, frc)
                bounds.width < maxLabelWidth && bounds.height < maxLabelHeight
            }
    }

    companion object {
        private const val OVERLAY_HEIGHT = 0.4f
    }
}

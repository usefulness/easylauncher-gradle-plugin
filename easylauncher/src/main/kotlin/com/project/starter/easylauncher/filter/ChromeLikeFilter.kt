package com.project.starter.easylauncher.filter

import com.project.starter.easylauncher.plugin.ADAPTIVE_CONTENT_SCALE
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
    ribbonColor: Color? = null,
    labelColor: Color? = null,
    private val labelPadding: Int? = null,
    overlayHeight: Float? = null,
    gravity: Gravity? = null,
    private val textSizeRatio: Float? = null,
    fontName: String? = null,
    fontResource: File? = null,
) : EasyLauncherFilter {

    enum class Gravity {
        TOP, BOTTOM
    }

    @Transient
    private val ribbonColor = ribbonColor ?: Color.DARK_GRAY

    @Transient
    private val labelColor = labelColor ?: Color.WHITE

    @Transient
    private val font = getFont(
        resource = fontResource,
        name = fontName,
    )

    @Transient
    private val overlayHeight = overlayHeight ?: OVERLAY_HEIGHT

    @Transient
    private val gravity = gravity ?: Gravity.BOTTOM

    override fun apply(canvas: Canvas, adaptive: Boolean) {
        canvas.use { graphics ->
            apply(canvas, graphics, adaptive)
        }
    }

    private fun apply(canvas: Canvas, graphics: Graphics2D, adaptive: Boolean) {
        val frc = FontRenderContext(graphics.transform, true, true)
        // calculate the rectangle where the label is rendered
        val backgroundHeight = (canvas.height * overlayHeight).roundToInt()
        graphics.font = getFont(
            imageHeight = canvas.height,
            maxLabelWidth = (canvas.width * ADAPTIVE_CONTENT_SCALE).roundToInt(),
            maxLabelHeight = (backgroundHeight * ADAPTIVE_CONTENT_SCALE).roundToInt(),
            frc = frc,
        )
        val textBounds = graphics.font.getStringBounds(label, frc)

        // draw the ribbon
        graphics.color = ribbonColor
        if (!adaptive) {
            graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f)
        }
        when (gravity) {
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
        graphics.color = labelColor
        val fm = graphics.fontMetrics
        when (gravity) {
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

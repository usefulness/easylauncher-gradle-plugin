package com.project.starter.easylauncher.filter

import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

internal const val ADAPTIVE_SCALE = 72 / 108f

/**
 * A canvas to draw onto.
 * @param width The width of the canvas.
 * @param height The height of the canvas.
 * @param graphics The [Graphics2D] used for actual drawing.
 */
class Canvas(
    val width: Int,
    val height: Int,
    val paddingTop: Int,
    val paddingBottom: Int,
    val paddingLeft: Int,
    val paddingRight: Int,
    private val graphics: Graphics2D,
) {

    /** Full canvas width (including paddings). */
    val fullWidth: Int
        get() = width + paddingLeft + paddingRight

    /** Full canvas height (including paddings). */
    val fullHeight: Int
        get() = height + paddingTop + paddingBottom

    /**
     * Use this Canvas once to execute all drawing operations.
     * The canvas is closed afterwards and can not be used again.
     */
    fun use(block: (Graphics2D) -> Unit) {
        graphics.apply(block).dispose()
    }
}

/**
 * Rotate this canvas by [angle] degrees around the point ([x], [y]).
 */
fun Canvas.rotate(graphics: Graphics2D, angle: Number, x: Number, y: Number) {
    graphics.translate(-paddingLeft, -paddingTop)
    graphics.transform = AffineTransform.getRotateInstance(
        Math.toRadians(angle.toDouble()),
        paddingLeft + x.toDouble(),
        paddingTop + y.toDouble(),
    )
    graphics.translate(paddingLeft, paddingTop)
}

/**
 * Create a [Canvas] for the given [image]
 *
 * @param image The image to create a canvas for.
 * @param adaptive `true` to create an adaptive canvas with a specific 18dp padding.
 */
fun Canvas(image: BufferedImage, adaptive: Boolean): Canvas {
    val width = image.getViewportWidth(adaptive)
    val height = image.getViewportHeight(adaptive)

    val paddingLeft = (image.width - width) / 2
    val paddingRight = image.width - width - paddingLeft
    val paddingTop = (image.height - height) / 2
    val paddingBottom = image.height - height - paddingTop

    val graphics = image.createGraphics().apply {
        translate(paddingLeft, paddingTop)
    }

    return Canvas(
        width = width,
        height = height,
        paddingTop = paddingTop,
        paddingBottom = paddingBottom,
        paddingLeft = paddingLeft,
        paddingRight = paddingRight,
        graphics = graphics,
    )
}

/**
 * Calculate the width of the viewport (e.g. the visible area of the image).
 *
 *  For legacy icons, this is simply the full image width, but for adaptive icons this is only the inner 72dp of a 108dp icon.
 */
private fun BufferedImage.getViewportWidth(adaptive: Boolean) = if (adaptive) (width * ADAPTIVE_SCALE).roundToInt() else width

/**
 * Calculate the height of the viewport (e.g. the visible area of the image).
 *
 *  For legacy icons, this is simply the full image height, but for adaptive icons this is only the inner 72dp of a 108dp icon.
 */
private fun BufferedImage.getViewportHeight(adaptive: Boolean) = if (adaptive) (height * ADAPTIVE_SCALE).roundToInt() else height

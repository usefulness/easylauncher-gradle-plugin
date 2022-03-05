package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.filter.ChromeLikeFilter
import com.project.starter.easylauncher.filter.ColorRibbonFilter
import com.project.starter.easylauncher.filter.OverlayFilter
import org.apache.commons.lang3.SerializationUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.awt.Color
import java.io.File
import java.io.Serializable

class SerializationTest {

    @Test
    fun `serializes chrome-like filter`() {
        test(
            ChromeLikeFilter(
                label = "fixture-label",
                gravity = ChromeLikeFilter.Gravity.TOP,
                labelColor = Color.MAGENTA,
                ribbonColor = Color.YELLOW,
                labelPadding = 10,
                overlayHeight = 0.6f,
                textSizeRatio = 0.2f,
                fontName = "ComicSans",
            ),
        )

        test(ChromeLikeFilter(label = "fixture-label"))
    }

    @Test
    fun `serializes color ribbon filter`() {
        test(
            ColorRibbonFilter(
                label = "fixture-label",
                gravity = ColorRibbonFilter.Gravity.BOTTOM,
                ribbonColor = Color.CYAN,
                labelColor = Color(1, 2, 3),
                textSizeRatio = 0.2f,
                fontName = "ComicSans",
                drawingOptions = setOf(ColorRibbonFilter.DrawingOption.IGNORE_TRANSPARENT_PIXELS),
            ),
        )

        test(ColorRibbonFilter(label = "fixture-label"))
    }

    @Test
    fun `serializes overlay filter`() =
        test(OverlayFilter(fgFile = File("")))

    private fun <T : Serializable> test(input: T) {
        val output = SerializationUtils.deserialize<T>(SerializationUtils.serialize(input))

        assertThat(output).usingRecursiveComparison().isEqualTo(input)
    }
}

package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.filter.ChromeLikeFilter
import com.project.starter.easylauncher.filter.ColorRibbonFilter
import com.project.starter.easylauncher.filter.OverlayFilter
import org.apache.commons.lang3.SerializationUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.io.Serializable

class SerializationTest {

    @Test
    fun `serializes chrome-like filter`() =
        test(ChromeLikeFilter(label = "", gravity = ChromeLikeFilter.Gravity.TOP))

    @Test
    fun `serializes color ribbon filter`() =
        test(ColorRibbonFilter(label = "", gravity = ColorRibbonFilter.Gravity.BOTTOM))

    @Test
    fun `serializes overlay filter`() =
        test(OverlayFilter(fgFile = File("")))

    private fun <T : Serializable> test(input: T) {
        val output = SerializationUtils.deserialize<T>(SerializationUtils.serialize(input))

        assertThat(output).usingRecursiveComparison().isEqualTo(input)
    }
}

package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.filter.ChromeLikeFilter
import com.project.starter.easylauncher.filter.ColorRibbonFilter
import com.project.starter.easylauncher.filter.OverlayFilter
import com.project.starter.easylauncher.plugin.utils.vectorFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.Color
import java.io.File

internal class IconTransformerTest {

    @TempDir
    lateinit var tempDir: File
    lateinit var sourceIcon: File
    lateinit var output: File

    @BeforeEach
    internal fun setUp() {
        val drawable = tempDir.resolve("drawable").apply {
            mkdir()
        }
        sourceIcon = drawable.resolve("icon_resource.xml")
        sourceIcon.writeText(vectorFile())

        output = drawable.resolve("output.xml")
    }

    @Test
    fun `transforms vector icon pre api 26`() {
        val expected = tempDir.resolve("drawable-anydpi-v26/output.xml")
        sourceIcon.transformXml(
            outputFile = output,
            minSdkVersion = 21,
            filters = listOf(
                ColorRibbonFilter(label = "test1", ribbonColor = Color.BLUE),
                ColorRibbonFilter(label = "test2", ribbonColor = Color.RED, gravity = ColorRibbonFilter.Gravity.BOTTOM),
                ChromeLikeFilter(label = "test3"),
                OverlayFilter(fgFile = File("src/test/resources/beta.png")),
            ),
        )

        assertThat(expected).hasContent(
            """
            |<?xml version="1.0" encoding="utf-8"?>
            |<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
            |
            |    <item android:drawable="@drawable/easy_icon_resource" />
            |
            |    <item android:drawable="@drawable/colorribbonfilter_0_output" />
            |
            |    <item android:drawable="@drawable/colorribbonfilter_1_output" />
            |
            |    <item android:drawable="@drawable/chromelikefilter_2_output" />
            |
            |    <item android:drawable="@drawable/overlayfilter_3_output" />
            |
            |</layer-list>
            |""".trimMargin(),
        )
    }

    @Test
    fun `transforms vector icon since api 26`() {
        val expected = tempDir.resolve("drawable-anydpi/output.xml")
        sourceIcon.transformXml(
            outputFile = output,
            minSdkVersion = 26,
            filters = listOf(
                ColorRibbonFilter(label = "test1", ribbonColor = Color.BLUE),
                ColorRibbonFilter(label = "test2", ribbonColor = Color.RED, gravity = ColorRibbonFilter.Gravity.BOTTOM),
                ChromeLikeFilter(label = "test3"),
                OverlayFilter(fgFile = File("src/test/resources/beta.png")),
            ),
        )

        assertThat(expected).hasContent(
            """
            |<?xml version="1.0" encoding="utf-8"?>
            |<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
            |
            |    <item android:drawable="@drawable/easy_icon_resource" />
            |
            |    <item android:drawable="@drawable/colorribbonfilter_0_output" />
            |
            |    <item android:drawable="@drawable/colorribbonfilter_1_output" />
            |
            |    <item android:drawable="@drawable/chromelikefilter_2_output" />
            |
            |    <item android:drawable="@drawable/overlayfilter_3_output" />
            |
            |</layer-list>
            |""".trimMargin(),
        )
    }
}

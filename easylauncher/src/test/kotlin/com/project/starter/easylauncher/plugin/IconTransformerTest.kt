package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.filter.ColorRibbonFilter
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
    fun `transforms vector icon`() {
        val expected = tempDir.resolve("drawable-anydpi-v26/output.xml")
        sourceIcon.transformXml(
            output,
            listOf(
                ColorRibbonFilter(label = "test1", ribbonColor = Color.BLUE),
                ColorRibbonFilter(label = "test2", ribbonColor = Color.RED, gravity = ColorRibbonFilter.Gravity.BOTTOM)
            )
        )

        assertThat(expected).hasContent(
            """
            |<?xml version="1.0" encoding="utf-8"?>
            |<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
            |
            |   <item android:drawable="@drawable/easy_icon_resource" />
            |   
            |   <item
            |       android:width="16dp"
            |       android:height="16dp"
            |       android:drawable="@drawable/colorribbonfilter_0_output"
            |       android:gravity="center"
            |       />
            |
            |   <item
            |       android:width="16dp"
            |       android:height="16dp"
            |       android:drawable="@drawable/colorribbonfilter_1_output"
            |       android:gravity="center"
            |       />
            |
            |</layer-list>
            |""".trimMargin()
        )
    }
}

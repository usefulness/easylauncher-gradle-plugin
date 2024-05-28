package com.starter.easylauncher.screenshot

import com.example.custom.adaptive.MainActivity
import com.starter.easylauncher.recordScreenshot
import org.junit.Test

internal class IconsTest {

    @Test
    fun doScreenshot() {
        recordScreenshot<MainActivity>(flavor = "insets")
    }
}

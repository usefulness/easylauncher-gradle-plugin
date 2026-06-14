package com.starter.easylauncher.screenshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.custom.adaptive.MainActivity
import com.example.simple.BuildConfig
import com.starter.easylauncher.recordScreenshot
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xhdpi")
internal class IconsTest {

    @Test
    fun doScreenshot() {
        recordScreenshot<MainActivity>(BuildConfig.BUILD_TYPE)
    }
}

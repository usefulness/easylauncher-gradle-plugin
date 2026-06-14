package com.starter.easylauncher.screenshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.multiplelauncher.BuildConfig
import com.example.multiplelauncher.MultiAliasActivity
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
        recordScreenshot<MultiAliasActivity>(BuildConfig.BUILD_TYPE)
    }
}

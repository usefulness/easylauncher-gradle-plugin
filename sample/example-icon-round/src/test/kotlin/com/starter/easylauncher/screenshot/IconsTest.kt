package com.starter.easylauncher.screenshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.custom.adaptive.MainActivity
import com.example.simple.BuildConfig
import com.example.simple.R
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
    fun default() {
        recordScreenshot<MainActivity>("${BuildConfig.FLAVOR}-default", iconName = R.mipmap.ic_launcher)
    }

    @Test
    fun round() {
        recordScreenshot<MainActivity>("${BuildConfig.FLAVOR}-round", iconName = R.mipmap.ic_launcher_round)
    }
}

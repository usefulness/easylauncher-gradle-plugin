package com.starter.easylauncher.screenshot

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import com.example.custom.adaptive.MainActivity
import com.example.simple.BuildConfig
import com.example.simple.R
import com.starter.easylauncher.recordScreenshot
import org.junit.Rule
import org.junit.Test

internal class IconsTest {

    @get:Rule
    val grantPermission = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Test
    fun default() {
        recordScreenshot<MainActivity>("${BuildConfig.FLAVOR}-default", iconName = R.mipmap.ic_launcher)
    }

    @Test
    fun round() {
        recordScreenshot<MainActivity>("${BuildConfig.FLAVOR}-round", iconName = R.mipmap.ic_launcher_round)
    }
}

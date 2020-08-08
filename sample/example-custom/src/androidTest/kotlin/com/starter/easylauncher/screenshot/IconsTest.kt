package com.starter.easylauncher.screenshot

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import com.example.custom.BuildConfig
import com.starter.easylauncher.recordScreenshot
import org.junit.Rule
import org.junit.Test

internal class IconsTest {

    @get:Rule
    val grantPermission = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Test
    fun doScreenshot() {
        recordScreenshot(BuildConfig.FLAVOR)
    }
}

package com.starter.easylauncher.screenshot

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import com.example.multiplelauncher.BuildConfig
import com.example.multiplelauncher.MultiAliasActivity
import com.starter.easylauncher.recordScreenshot
import org.junit.Rule
import org.junit.Test

internal class IconsTest {

    @get:Rule
    val grantPermission = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Test
    fun doScreenshot() {
        recordScreenshot<MultiAliasActivity>(BuildConfig.BUILD_TYPE)
    }
}

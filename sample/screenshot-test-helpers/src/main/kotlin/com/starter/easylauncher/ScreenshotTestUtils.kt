package com.starter.easylauncher

import android.view.View
import androidx.test.core.app.launchActivity
import com.example.custom.adaptive.MainActivity
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import com.facebook.testing.screenshot.internal.TestNameDetector

private const val SCREENSHOT_WIDTH = 300

fun recordScreenshot(flavor: String) {
    lateinit var root: View
    launchActivity<MainActivity>().onActivity { activity ->
        root = activity.findViewById<View>(android.R.id.content)
    }

    ViewHelpers.setupView(root).setExactWidthDp(SCREENSHOT_WIDTH).layout()

    root.record(flavor)
}

private fun View.record(flavor: String) {
    val testClassText = TestNameDetector.getTestClass().substringAfterLast('.').removeSuffix("Test")

    Screenshot
        .snap(this)
        .setGroup(flavor)
        .setName("${testClassText}_${TestNameDetector.getTestName()}($flavor)")
        .record()
}

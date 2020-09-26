package com.starter.easylauncher

import android.app.Activity
import android.view.View
import androidx.test.core.app.ActivityScenario
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import com.facebook.testing.screenshot.internal.TestNameDetector
import kotlin.reflect.KClass

private const val SCREENSHOT_WIDTH = 300

inline fun <reified T : Activity> recordScreenshot(flavor: String) = recordScreenshot(T::class, flavor)

fun recordScreenshot(activityClass: KClass<out Activity>, flavor: String) {
    lateinit var root: View
    ActivityScenario.launch(activityClass.java).onActivity { activity ->
        root = activity.findViewById(android.R.id.content)
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

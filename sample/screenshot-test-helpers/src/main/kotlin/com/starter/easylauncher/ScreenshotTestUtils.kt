package com.starter.easylauncher

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.screenshot.helpers.R
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import com.facebook.testing.screenshot.internal.TestNameDetector
import kotlin.reflect.KClass

private const val SCREENSHOT_WIDTH = 300

inline fun <reified T : Activity> recordScreenshot(flavor: String, @DrawableRes iconName: Int = R.mipmap.ic_launcher) =
    recordScreenshot(T::class, flavor, iconName)

fun recordScreenshot(activityClass: KClass<out Activity>, flavor: String, @DrawableRes iconName: Int) {
    lateinit var root: View
    val startIntent = Intent(ApplicationProvider.getApplicationContext(), activityClass.java)
        .putExtra("iconName", iconName)
    ActivityScenario.launch<Activity>(startIntent).onActivity { activity ->
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

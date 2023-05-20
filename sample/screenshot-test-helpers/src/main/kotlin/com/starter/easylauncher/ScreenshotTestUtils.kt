package com.starter.easylauncher

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.TestNameDetector
import com.facebook.testing.screenshot.ViewHelpers
import kotlin.reflect.KClass
import com.example.custom.adaptive.R as AdaptiveR

private const val SCREENSHOT_WIDTH = 300

inline fun <reified T : Activity> recordScreenshot(flavor: String, @DrawableRes iconName: Int = AdaptiveR.mipmap.ic_launcher) =
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
    val methodInfo = TestNameDetector.getTestMethodInfo().let(::checkNotNull)
    val testClassText = methodInfo.className.substringAfterLast('.').removeSuffix("Test")

    Screenshot
        .snap(this)
        .setGroup(flavor)
        .setName("${testClassText}_${methodInfo.methodName}($flavor)")
        .record()
}

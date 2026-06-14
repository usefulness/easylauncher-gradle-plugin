package com.starter.easylauncher

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.dropbox.differ.SimpleImageComparator
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import kotlin.reflect.KClass
import com.example.custom.adaptive.R as AdaptiveR

private const val SCREENSHOT_WIDTH_DP = 300

// Robolectric's native rendering produces tiny (delta <= ~3/255) anti-aliasing differences across
// host OSes, so goldens recorded on one OS would fail verification on another (e.g. macOS vs CI Linux).
// A per-pixel colour tolerance absorbs that noise while still catching any real change to the icon -
// applying/removing a ribbon swaps colours far beyond this distance, regardless of how small the icon is.
private val roborazziOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(
        imageComparator = SimpleImageComparator(maxDistance = 0.05f),
    ),
)

inline fun <reified T : Activity> recordScreenshot(flavor: String, @DrawableRes iconName: Int = AdaptiveR.mipmap.ic_launcher) =
    recordScreenshot(T::class, flavor, iconName)

fun recordScreenshot(activityClass: KClass<out Activity>, flavor: String, @DrawableRes iconName: Int) {
    val startIntent = Intent(ApplicationProvider.getApplicationContext(), activityClass.java)
        .putExtra("iconName", iconName)

    ActivityScenario.launch<Activity>(startIntent).use { scenario ->
        scenario.onActivity { activity ->
            val root = activity.findViewById<View>(android.R.id.content)

            // Capture at a fixed width with wrap-content height, independent of the device window size.
            val widthPx = (SCREENSHOT_WIDTH_DP * activity.resources.displayMetrics.density).toInt()
            root.measure(
                View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            )
            root.layout(0, 0, root.measuredWidth, root.measuredHeight)

            root.captureRoboImage(filePath = "screenshots/${flavor.toFileName()}.png", roborazziOptions = roborazziOptions)
        }
    }
}

// Golden file names must be filesystem-friendly; the flavor label is unique per golden within a module.
private fun String.toFileName() = replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')

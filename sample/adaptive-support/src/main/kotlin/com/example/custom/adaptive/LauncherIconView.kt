package com.example.custom.adaptive

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

class LauncherIconView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.launcher_icon_view, this)
    }

    private val container = findViewById<ViewGroup>(R.id.adaptive_container)
    private val backgroundLayer = findViewById<ImageView>(R.id.img_background)
    private val foregroundLayer = findViewById<ImageView>(R.id.img_foreground)
    private val legacyView = findViewById<ImageView>(R.id.img_legacy)

    fun setIcon(@DrawableRes iconResource: Int) {
        val adaptive = getAdaptiveIcon(iconResource)
        adaptive?.setupAdaptiveIcon() ?: setupLegacyIcon(iconResource)
    }

    private fun getAdaptiveIcon(@DrawableRes iconResource: Int): AdaptiveIconDrawable? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.getDrawable(context, iconResource) as? AdaptiveIconDrawable
        } else {
            null
        }
    }

    @SuppressLint("NewApi") // whole AdaptiveIconDrawable is available since API26
    private fun AdaptiveIconDrawable.setupAdaptiveIcon() {
        container.isVisible = true
        legacyView.isVisible = false
        backgroundLayer.setImageDrawable(background)
        foregroundLayer.setImageDrawable(foreground)
    }

    private fun setupLegacyIcon(@DrawableRes iconResource: Int) {
        container.isVisible = false
        legacyView.isVisible = true
        legacyView.setImageResource(iconResource)
    }
}

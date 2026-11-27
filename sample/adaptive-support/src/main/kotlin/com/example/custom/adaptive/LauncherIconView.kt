package com.example.custom.adaptive

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

class LauncherIconView(
    context: Context,
    attrs: AttributeSet?,
) : LinearLayout(context, attrs) {

    init {
        inflate(context, R.layout.launcher_icon_view, this)
    }

    private val container = findViewById<ViewGroup>(R.id.adaptive_container)
    private val backgroundLayer = findViewById<ImageView>(R.id.img_background)
    private val foregroundLayer = findViewById<ImageView>(R.id.img_foreground)
    private val legacyView = findViewById<ImageView>(R.id.img_legacy)
    private val themedLight = ThemedPreview(
        container = findViewById(R.id.themed_light_container),
        background = findViewById(R.id.img_themed_light_background),
        monochrome = findViewById(R.id.img_themed_light_monochrome),
        backgroundColor = 0xFFD3E3FD.toInt(),
        foregroundColor = 0xFF041E49.toInt(),
    )
    private val themedDark = ThemedPreview(
        container = findViewById(R.id.themed_dark_container),
        background = findViewById(R.id.img_themed_dark_background),
        monochrome = findViewById(R.id.img_themed_dark_monochrome),
        backgroundColor = 0xFF1F1F1F.toInt(),
        foregroundColor = 0xFFD3E3FD.toInt(),
    )

    fun setIcon(@DrawableRes iconResource: Int) {
        val adaptive = getAdaptiveIcon(iconResource)
        adaptive?.setupAdaptiveIcon() ?: setupLegacyIcon(iconResource)
    }

    private fun getAdaptiveIcon(@DrawableRes iconResource: Int): AdaptiveIconDrawable? = if (Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.O
    ) {
        ContextCompat.getDrawable(context, iconResource) as? AdaptiveIconDrawable
    } else {
        null
    }

    @SuppressLint("NewApi") // whole AdaptiveIconDrawable is available since API26
    private fun AdaptiveIconDrawable.setupAdaptiveIcon() {
        container.isVisible = true
        legacyView.isVisible = false
        backgroundLayer.setImageDrawable(background)
        foregroundLayer.setImageDrawable(foreground)
        val monochromeLayer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) monochrome else null
        themedLight.show(monochromeLayer)
        themedDark.show(monochromeLayer)
    }

    private fun setupLegacyIcon(@DrawableRes iconResource: Int) {
        container.isVisible = false
        legacyView.isVisible = true
        legacyView.setImageResource(iconResource)
    }
}

private class ThemedPreview(
    private val container: ViewGroup,
    private val background: ImageView,
    private val monochrome: ImageView,
    private val backgroundColor: Int,
    private val foregroundColor: Int,
) {

    fun show(layer: Drawable?) {
        container.isVisible = layer != null
        layer ?: return
        background.setImageDrawable(ColorDrawable(backgroundColor))
        monochrome.setImageDrawable(layer.constantState!!.newDrawable().mutate().apply { setTint(foregroundColor) })
    }
}

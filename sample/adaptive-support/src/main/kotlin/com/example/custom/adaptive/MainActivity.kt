package com.example.custom.adaptive

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        StrictMode.enableDefaults()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<View>(R.id.button).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hello, Android!")
                .setMessage("This is an example app with easylaucher icon applied")
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }

        val adaptive = getAdaptiveIcon()
        adaptive?.setupAdaptiveIcon() ?: setupLegacyIcon()
    }

    private fun getAdaptiveIcon(): AdaptiveIconDrawable? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getDrawable(R.mipmap.ic_launcher) as? AdaptiveIconDrawable
        } else {
            null
        }
    }

    @SuppressLint("NewApi") // whole AdaptiveIconDrawable is available since API26
    private fun AdaptiveIconDrawable.setupAdaptiveIcon() {
        val container = findViewById<ViewGroup>(R.id.adaptive_container)
        val backgroundLayer = findViewById<ImageView>(R.id.img_background)
        val foregroundLayer = findViewById<ImageView>(R.id.img_foreground)
        container.isVisible = true
        backgroundLayer.setImageDrawable(background)
        foregroundLayer.setImageDrawable(foreground)
    }

    private fun setupLegacyIcon() {
        findViewById<ImageView>(R.id.img_legacy).isVisible = true
    }
}

package com.example.custom.adaptive

import android.app.AlertDialog
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import androidx.appcompat.app.AppCompatActivity

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
        val iconView = findViewById<LauncherIconView>(R.id.launcher_icon)
        iconView.setIcon(R.mipmap.ic_launcher)
    }
}

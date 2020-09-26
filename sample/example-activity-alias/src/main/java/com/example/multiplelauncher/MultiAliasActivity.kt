package com.example.multiplelauncher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.custom.adaptive.LauncherIconView

class MultiAliasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alias_main)

        findViewById<LauncherIconView>(R.id.launcher_icon).apply {
            setIcon(R.mipmap.ic_launcher)
        }
        findViewById<LauncherIconView>(R.id.fist_alias_icon).apply {
            setIcon(R.mipmap.ic_launcher_one)
        }
        findViewById<LauncherIconView>(R.id.second_alias_icon).apply {
            setIcon(R.mipmap.ic_launcher_two)
        }
    }
}

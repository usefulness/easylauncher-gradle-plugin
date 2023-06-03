package com.project.starter.easylauncher.filter

import java.io.Serializable

interface EasyLauncherFilter : Serializable {

    fun apply(canvas: Canvas, modifier: Modifier?)

    enum class Modifier {
        Adaptive,
        Round,
    }
}

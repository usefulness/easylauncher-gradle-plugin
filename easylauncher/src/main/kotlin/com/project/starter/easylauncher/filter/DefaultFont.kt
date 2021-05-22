package com.project.starter.easylauncher.filter

import java.awt.Font

internal val DEFAULT_EASYLAUNCHER_FONT: Font by lazy {
    EasyLauncherFilter::class.java.classLoader
        .getResourceAsStream("Roboto-Regular.ttf").use {
            Font.createFont(Font.TRUETYPE_FONT, it)
        }
}

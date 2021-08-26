package com.project.starter.easylauncher.filter

import java.awt.Font

internal val DEFAULT_EASYLAUNCHER_FONT: Font by lazy {
    EasyLauncherFilter::class.java.classLoader
        .getResourceAsStream("Roboto-Regular.ttf")
        .use {
            runCatching { Font.createFont(Font.TRUETYPE_FONT, it) }
                .getOrNull()
                ?: error("Couldn't load bundled font. Make sure you have font support installed.\nhttps://github.com/usefulness/easylauncher-gradle-plugin/issues/201")
        }
}

package com.project.starter.easylauncher.filter

import java.awt.Font
import java.io.File

private object FontLoader

private val EASYLAUNCHER_DEFAULT_FONT by lazy {
    FontLoader::class.java.classLoader
        .getResourceAsStream("Roboto-Regular.ttf")
        .use {
            runCatching { Font.createFont(Font.TRUETYPE_FONT, it) }
                .getOrElse { error("Couldn't load bundled font. Visit issue #201 for more details") }
        }
}

internal fun findFontFile(
    resource: File?,
    name: String?,
): Font {
    if (resource != null) {
        if (!resource.exists()) {
            error("${resource.path} does not exits. Make sure it points at existing font resource.")
        }

        return runCatching { Font.createFont(Font.TRUETYPE_FONT, resource) }
            .getOrElse { throw IllegalStateException("Error while loading ${resource.path}", it) }
    }

    if (name != null) {
        return runCatching { Font.decode(name) }
            .getOrElse { throw IllegalStateException("Couldn't load font $name.", it) }
    }

    return EASYLAUNCHER_DEFAULT_FONT
}

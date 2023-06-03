package com.project.starter.easylauncher.plugin.internal

import java.util.Locale

internal fun String.uppercase() = toUpperCase(Locale.ROOT)

internal fun String.lowercase() = toLowerCase(Locale.ROOT)

internal fun String.replaceFirstChar(block: (Char) -> Char): String = if (this.isEmpty()) {
    this
} else {
    "${block(first())}${substring(1)}"
}

internal fun Char.titleCase() = this.toUpperCase()

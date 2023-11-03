package com.project.starter.easylauncher.plugin.models

import kotlin.math.roundToInt

internal data class Size(
    val value: Int,
    val unit: String,
)

private val sizeFormat by lazy { "(\\d*)(\\D*)".toRegex() }

internal fun String.toSize(): Size {
    val groupValues = sizeFormat.matchEntire(this)?.groupValues.let(::requireNotNull)

    return Size(groupValues[1].toFloat().roundToInt(), groupValues[2])
}

package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.plugin.models.IconFile
import com.project.starter.easylauncher.plugin.models.IconType
import com.project.starter.easylauncher.plugin.models.toSize
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import java.io.File

internal fun File.getLauncherIcons(manifestPlaceholders: Map<String, Any> = emptyMap()): Map<String, IconType> {
    val manifestXml = XmlSlurper().parse(this)
    val applicationNode = manifestXml.getProperty("application") as GPathResult
    val icon = applicationNode.getProperty("@android:icon")?.toString()?.applyPlaceholders(manifestPlaceholders)
    val roundIcon = applicationNode.getProperty("@android:roundIcon")?.toString()?.applyPlaceholders(manifestPlaceholders)

    return mutableMapOf<String, IconType>().apply {
        icon?.takeIf { it.isNotBlank() }?.let { put(it, IconType.Default) }
        roundIcon?.takeIf { it.isNotBlank() }?.let { put(it, IconType.Round) }
    }
}

private val regex by lazy { "\\\$\\{([^{}]*)}".toRegex() }

private fun String.applyPlaceholders(manifestPlaceholders: Map<String, Any>): String =
    replace(regex) { manifestPlaceholders[it.groups[1]?.value]?.toString() ?: it.value }

internal fun File.tryParseXmlIcon(): IconFile? {
    if (extension != "xml") {
        return null
    }

    val iconXml = XmlSlurper().parse(this)
    val foreground = iconXml.getProperty("foreground") as GPathResult
    val background = iconXml.getProperty("background") as GPathResult
    val monochrome = iconXml.getProperty("monochrome") as GPathResult
    val backgroundDrawable = background.property("@android:drawable")
    val foregroundDrawable = foreground.property("@android:drawable")
    val monochromeDrawable = monochrome.property("@android:drawable")

    return if (backgroundDrawable != null && foregroundDrawable != null) {
        IconFile.Adaptive(
            file = this,
            background = backgroundDrawable,
            foreground = foregroundDrawable,
            monochrome = monochromeDrawable,
        )
    } else {
        tryParseXmlDrawable()
    }
}

internal fun File.tryParseXmlDrawable(): IconFile.XmlDrawable? {
    val iconXml = XmlSlurper().parse(this)
    val width = iconXml.property("@android:width")?.toSize()?.value
    val height = iconXml.property("@android:height")?.toSize()?.value

    return when {
        width != null && height != null -> IconFile.XmlDrawable.Vector(
            file = this,
            width = width,
            height = height,
        )

        else -> null
    }
}

internal fun GPathResult.property(key: String) = getProperty(key)?.toString()?.takeIf { it.isNotBlank() }

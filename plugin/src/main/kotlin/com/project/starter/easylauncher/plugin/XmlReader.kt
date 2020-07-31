package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.plugin.models.AdaptiveIcon
import groovy.util.XmlSlurper
import groovy.util.slurpersupport.GPathResult
import java.io.File

internal fun File.getLauncherIcons(): String? {
    val manifestXml = XmlSlurper().parse(this)
    val applicationNode = manifestXml.getProperty("application") as GPathResult
    val icon = applicationNode.getProperty("@android:icon")?.toString()

    return icon?.takeIf { it.isNotBlank() }
}

internal fun File.asAdaptiveIcon(): AdaptiveIcon? {
    if (extension != "xml") {
        return null
    }

    val iconXml = XmlSlurper().parse(this)
    val foreground = iconXml.getProperty("foreground") as GPathResult
    val background = iconXml.getProperty("foreground") as GPathResult
    val backgroundDrawable = background.property("@android:drawable")
    val foregroundDrawable = foreground.property("@android:drawable")

    return if (backgroundDrawable != null && foregroundDrawable != null) {
        AdaptiveIcon(
            file = this,
            background = backgroundDrawable,
            foreground = foregroundDrawable
        )
    } else {
        null
    }
}

internal fun GPathResult.property(key: String) =
    getProperty(key)?.toString()?.takeIf { it.isNotBlank() }

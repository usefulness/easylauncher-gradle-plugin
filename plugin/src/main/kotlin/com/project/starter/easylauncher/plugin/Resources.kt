package com.project.starter.easylauncher.plugin

import groovy.util.XmlSlurper
import groovy.util.slurpersupport.GPathResult
import java.io.File
import java.util.Collections

object Resources {

    fun resourceFilePattern(name: String): String {
        return if (name.startsWith("@")) {
            val pair = name.substring(1).split("/".toRegex(), 2).toTypedArray()
            val baseResType = pair[0]
            val fileName = pair.getOrNull(1)
                ?: throw IllegalArgumentException("Icon names does include resource types (e.g. drawable/ic_launcher): $name")
            "$baseResType*/$fileName.*"
        } else {
            name
        }
    }

    fun getLauncherIcons(manifestFile: File?): List<String> {
        val manifestXml = XmlSlurper().parse(manifestFile)
        val applicationNode = manifestXml.getProperty("application") as GPathResult
        val icon = applicationNode.getProperty("@android:icon").toString()
        val roundIcon = applicationNode.getProperty("@android:roundIcon").toString()
        val icons = mutableListOf<String>()
        if (icon.isNotEmpty()) {
            icons.add(icon)
        }
        if (roundIcon.isNotEmpty()) {
            icons.add(roundIcon)
        }
        return Collections.unmodifiableList(icons)
    }
}

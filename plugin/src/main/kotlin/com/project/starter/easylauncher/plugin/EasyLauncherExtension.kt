package com.project.starter.easylauncher.plugin

import java.util.Arrays

internal open class EasyLauncherExtension {

    companion object {
        const val NAME = "easylauncher"
    }

    var iconNames = mutableSetOf<String>()
        private set
    var foregroundIconNames = mutableSetOf<String>()
        private set

    /**
     * True to use flavor name for default ribbons, false to use type name
     */
    /**
     * @param defaultFlavorNaming true to use flavor name for default ribbons, false to use type name
     */
    var isDefaultFlavorNaming = false

    /**
     * @param resNames Names of icons. For example "@drawable/ic_launcher", "@mipmap/icon"
     */
    fun setIconNames(resNames: Collection<String>?) {
        iconNames = resNames.orEmpty().toMutableSet()
    }

    /**
     * @param resNames Names of icons. For example "@drawable/ic_launcher", "@mipmap/icon"
     */
    fun iconNames(resNames: Collection<String>?) {
        setIconNames(resNames)
    }

    /**
     * @param resNames Names of icons. For example "@drawable/ic_launcher", "@mipmap/icon"
     */
    fun iconNames(vararg resNames: String?) {
        setIconNames(Arrays.asList<String>(*resNames))
    }

    /**
     * @param resName A name of icons. For example "@drawable/ic_launcher", "@mipmap/icon"
     */
    fun iconName(resName: String) {
        iconNames.add(resName)
    }

    /**
     * @param resNames Names of icons. For example "@drawable/ic_launcher", "@mipmap/icon"
     */
    fun setForegroundIconNames(resNames: Collection<String>?) {
        foregroundIconNames = resNames.orEmpty().toMutableSet()
    }

    /**
     * @param resNames Names of icons. For example "@drawable/ic_launcher", "@mipmap/icon"
     */
    fun foregroundIconNames(resNames: Collection<String>?) {
        setForegroundIconNames(resNames)
    }

    /**
     * @param resNames Names of icons. For example "@drawable/ic_launcher", "@mipmap/icon"
     */
    fun foregroundIconNames(vararg resNames: String?) {
        setForegroundIconNames(Arrays.asList<String>(*resNames))
    }

    /**
     * @param resName A name of icons. For example "@drawable/ic_launcher", "@mipmap/icon"
     */
    fun foregroundIconName(resName: String) {
        foregroundIconNames.add(resName)
    }

    /**
     * @param defaultFlavorNaming true to use flavor name for default ribbons, false to use type name
     */
    fun defaultFlavorNaming(defaultFlavorNaming: Boolean) {
        isDefaultFlavorNaming = defaultFlavorNaming
    }
}

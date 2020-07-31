package com.project.starter.easylauncher.plugin

internal open class EasyLauncherExtension {

    companion object {
        const val NAME = "easylauncher"
    }

    /**
     * True to use flavor name for default ribbons, false to use type name
     */
    /**
     * @param defaultFlavorNaming true to use flavor name for default ribbons, false to use type name
     */
    var isDefaultFlavorNaming = false

    /**
     * @param defaultFlavorNaming true to use flavor name for default ribbons, false to use type name
     */
    fun defaultFlavorNaming(defaultFlavorNaming: Boolean) {
        isDefaultFlavorNaming = defaultFlavorNaming
    }
}

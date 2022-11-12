package com.project.starter.easylauncher.plugin

import com.android.build.api.component.analytics.AnalyticsEnabledApplicationVariant
import com.android.build.api.dsl.AndroidSourceDirectorySet
import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.component.ComponentCreationConfig
import org.gradle.api.model.ObjectFactory
import java.io.File

internal fun ObjectFactory.getIconFiles(parent: File, iconName: String): Iterable<File> =
    fileTree().from(parent).apply {
        include(resourceFilePattern(iconName))
    }

private fun resourceFilePattern(name: String): String {
    return if (name.startsWith("@")) {
        val (baseResType, fileName) = name.substring(1).split("/".toRegex(), 2)
        "$baseResType*/$fileName.*"
    } else {
        name
    }
}

/**
 * Workaround for https://issuetracker.google.com/issues/197121905
 */
internal val Variant.isDebuggable: Boolean
    get() = when (this) {
        is AnalyticsEnabledApplicationVariant -> delegate.isDebuggable
        is ComponentCreationConfig -> debuggable
        else -> false
    }

@Suppress("DEPRECATION") // https://issuetracker.google.com/issues/170650362
internal val AndroidSourceDirectorySet.srcDirs
    get() = (this as? com.android.build.gradle.api.AndroidSourceDirectorySet)?.srcDirs.orEmpty()

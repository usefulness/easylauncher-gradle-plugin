package com.project.starter.easylauncher.plugin

import org.gradle.api.model.ObjectFactory
import java.io.File

internal fun ObjectFactory.getIconFiles(parent: File, iconName: String): Iterable<File> = fileTree().from(parent).apply {
    include(resourceFilePattern(iconName))
}

private fun resourceFilePattern(name: String): String = if (name.startsWith("@")) {
    val (baseResType, fileName) = name.substring(1).split("/".toRegex(), 2)
    "$baseResType*/$fileName.*"
} else {
    name
}

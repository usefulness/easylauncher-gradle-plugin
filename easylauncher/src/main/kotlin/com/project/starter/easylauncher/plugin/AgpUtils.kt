package com.project.starter.easylauncher.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import java.io.File

private val supportedPlugins = listOf(
    "com.android.application",
    "com.android.library",
)

internal fun Project.configureSupportedPlugins(block: (DomainObjectSet<out BaseVariant>) -> Unit) {
    supportedPlugins.forEach { pluginId ->
        pluginManager.withPlugin(pluginId) { block(findVariants()) }
    }
}

internal fun Project.findVariants(): DomainObjectSet<out BaseVariant> =
    extensions.findByType(AppExtension::class.java)?.applicationVariants
        ?: extensions.findByType(LibraryExtension::class.java)?.libraryVariants
        ?: this.objects.domainObjectSet(BaseVariant::class.java)

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

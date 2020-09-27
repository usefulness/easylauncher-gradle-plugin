package com.project.starter.easylauncher.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

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

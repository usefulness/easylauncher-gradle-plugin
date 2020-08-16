package com.project.starter.easylauncher.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

internal open class EasyLauncherExtension @Inject constructor(
    objectFactory: ObjectFactory
) {

    companion object {
        const val NAME = "easylauncher"
    }

    /**
     * True to use flavor name for default ribbons, false to use type name
     */
    /**
     * @param defaultFlavorNaming true to use flavor name for default ribbons, false to use type name
     */
    var isDefaultFlavorNaming: Property<Boolean> = objectFactory.property(Boolean::class.java).apply {
        set(false)
    }

    val buildTypes: NamedDomainObjectContainer<EasyLauncherConfig> =
        objectFactory.domainObjectContainer(EasyLauncherConfig::class.java)

    val productFlavors: NamedDomainObjectContainer<EasyLauncherConfig> =
        objectFactory.domainObjectContainer(EasyLauncherConfig::class.java)

    val variants: NamedDomainObjectContainer<EasyLauncherConfig> =
        objectFactory.domainObjectContainer(EasyLauncherConfig::class.java)

    /**
     * @param defaultFlavorNaming true to use flavor name for default ribbons, false to use type name
     */
    fun defaultFlavorNaming(defaultFlavorNaming: Boolean) {
        isDefaultFlavorNaming.value(defaultFlavorNaming)
    }
}

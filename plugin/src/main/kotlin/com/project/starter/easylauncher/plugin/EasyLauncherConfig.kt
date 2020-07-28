package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.filter.ColorRibbonFilter
import com.project.starter.easylauncher.filter.EasyLauncherFilter
import com.project.starter.easylauncher.filter.GrayscaleFilter
import com.project.starter.easylauncher.filter.OverlayFilter
import java.awt.Color
import java.io.File
import java.io.Serializable

@Suppress("MagicNumber", "TooManyFunctions")
open class EasyLauncherConfig(var name: String) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    var enabled = true
        private set
    val filters = mutableListOf<EasyLauncherFilter>()

    fun enable(enabled: Boolean?): EasyLauncherConfig {
        this.enabled = enabled!!
        return this
    }

    fun setFilters(filters: Iterable<EasyLauncherFilter>): EasyLauncherConfig {
        this.filters.addAll(filters)
        return this
    }

    fun setFilters(filter: EasyLauncherFilter): EasyLauncherConfig {
        filters.add(filter)
        return this
    }

    private fun filters(vararg filters: EasyLauncherFilter) {
        this.filters.addAll(filters)
    }

    //region Filters
    fun customColorRibbonFilter(
        name: String?,
        ribbonColor: String?,
        labelColor: String?,
        position: String,
        textSizeRatio: Float
    ): ColorRibbonFilter {
        return ColorRibbonFilter(
            name,
            Color.decode(ribbonColor),
            Color.decode(labelColor),
            ColorRibbonFilter.Gravity.valueOf(position.toUpperCase()),
            textSizeRatio
        )
    }

    fun customColorRibbonFilter(
        name: String?,
        ribbonColor: String?,
        labelColor: String?,
        gravity: String
    ): ColorRibbonFilter {
        return ColorRibbonFilter(
            name,
            Color.decode(ribbonColor),
            Color.decode(labelColor),
            ColorRibbonFilter.Gravity.valueOf(gravity.toUpperCase())
        )
    }

    fun customColorRibbonFilter(name: String?, ribbonColor: String?, labelColor: String?): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color.decode(ribbonColor), Color.decode(labelColor))
    }

    fun customColorRibbonFilter(name: String?, ribbonColor: String?): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color.decode(ribbonColor))
    }

    fun customColorRibbonFilter(ribbonColor: String?): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color.decode(ribbonColor))
    }

    fun grayRibbonFilter(name: String?): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0x60, 0x60, 0x60, 0x99))
    }

    fun grayRibbonFilter(): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0x60, 0x60, 0x60, 0x99))
    }

    fun greenRibbonFilter(name: String?): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0, 0x72, 0, 0x99))
    }

    fun greenRibbonFilter(): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0, 0x72, 0, 0x99))
    }

    fun orangeRibbonFilter(name: String?): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0xff, 0x76, 0, 0x99))
    }

    fun orangeRibbonFilter(): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0xff, 0x76, 0, 0x99))
    }

    fun yellowRibbonFilter(name: String?): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0xff, 251, 0, 0x99))
    }

    fun yellowRibbonFilter(): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0xff, 251, 0, 0x99))
    }

    fun redRibbonFilter(name: String?): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0xff, 0, 0, 0x99))
    }

    fun redRibbonFilter(): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0xff, 0, 0, 0x99))
    }

    fun blueRibbonFilter(name: String?): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0, 0, 255, 0x99))
    }

    fun blueRibbonFilter(): ColorRibbonFilter {
        return ColorRibbonFilter(name, Color(0, 0, 255, 0x99))
    }

    fun overlayFilter(fgFile: File): OverlayFilter {
        return OverlayFilter(fgFile)
    }

    fun grayscaleFilter(): GrayscaleFilter {
        return GrayscaleFilter()
    } //endregion
}

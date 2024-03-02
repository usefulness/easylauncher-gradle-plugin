package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.filter.ChromeLikeFilter
import com.project.starter.easylauncher.filter.ColorRibbonFilter
import com.project.starter.easylauncher.filter.EasyLauncherFilter
import com.project.starter.easylauncher.filter.OverlayFilter
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Nested
import java.awt.Color
import java.io.File
import java.io.Serializable
import javax.inject.Inject
import kotlin.math.roundToInt

open class EasyLauncherConfig @Inject constructor(
    val name: String,
    objectFactory: ObjectFactory,
) : Serializable {

    val enabled: Property<Boolean> = objectFactory.property(Boolean::class.java).apply {
        set(true)
    }

    @Nested
    internal val filters: SetProperty<EasyLauncherFilter> = objectFactory.setProperty(EasyLauncherFilter::class.java).apply {
        set(emptyList())
    }

    val iconNames: ListProperty<String> = objectFactory.listProperty(String::class.java).apply {
        set(emptyList())
    }

    fun enable(enabled: Boolean) {
        this.enabled.value(enabled)
    }

    fun setFilters(filters: Iterable<EasyLauncherFilter>) {
        this.filters.addAll(filters)
    }

    fun setFilters(filter: EasyLauncherFilter) {
        this.filters.value(this.filters.get() + filter)
    }

    fun filters(vararg filters: EasyLauncherFilter) {
        this.filters.value(this.filters.get() + filters)
    }

    fun setIconNames(names: Iterable<String>) {
        iconNames.set(names)
    }

    @JvmOverloads
    @Deprecated("use customRibbon method instead")
    fun customColorRibbonFilter(
        name: String? = null,
        ribbonColor: String?,
        labelColor: String = "#FFFFFF",
        position: String = "topleft",
        textSizeRatio: Float? = null,
    ): ColorRibbonFilter = customRibbon(
        label = name,
        ribbonColor = ribbonColor,
        labelColor = labelColor,
        gravity = ColorRibbonFilter.Gravity.valueOf(position.uppercase()),
        textSizeRatio = textSizeRatio,
    )

    fun customRibbon(properties: Map<String, Any>): ColorRibbonFilter {
        val label = properties["label"]?.toString()
        val ribbonColor = properties["ribbonColor"]?.toString()
        val labelColor = properties["labelColor"]?.toString()
        val position = properties["position"]?.toString()
            ?.uppercase()
            ?.let(ColorRibbonFilter.Gravity::valueOf)
        val textSizeRatio = properties["textSizeRatio"]?.toString()?.toFloatOrNull()
        val drawingOptions = (properties["drawingOptions"] as? Iterable<*>).toDrawingOptions()

        val fontName: String?
        val font: File?
        when (val fontRaw = properties["font"]) {
            is File -> {
                fontName = null
                font = fontRaw
            }

            is String -> {
                fontName = fontRaw
                font = null
            }

            else -> {
                fontName = properties["fontName"]?.toString()
                font = null
            }
        }

        return customRibbon(
            label = label,
            ribbonColor = ribbonColor,
            labelColor = labelColor,
            gravity = position,
            textSizeRatio = textSizeRatio,
            fontName = fontName,
            font = font,
            drawingOptions = drawingOptions,
        )
    }

    fun customRibbon(
        label: String? = null,
        ribbonColor: String? = null,
        labelColor: String? = null,
        gravity: ColorRibbonFilter.Gravity? = null,
        textSizeRatio: Float? = null,
        fontName: String? = null,
        font: File? = null,
        drawingOptions: Set<ColorRibbonFilter.DrawingOption> = emptySet(),
    ) = ColorRibbonFilter(
        label = label ?: name,
        ribbonColor = ribbonColor,
        labelColor = labelColor,
        gravity = gravity,
        textSizeRatio = textSizeRatio,
        fontName = fontName,
        fontResource = font,
        drawingOptions = drawingOptions,
    )

    @JvmOverloads
    fun grayRibbonFilter(label: String? = null) = ColorRibbonFilter(label ?: this.name, Color(0x60, 0x60, 0x60, 0x99).toHexString())

    @JvmOverloads
    fun greenRibbonFilter(label: String? = null) = ColorRibbonFilter(label ?: this.name, Color(0, 0x72, 0, 0x99).toHexString())

    @JvmOverloads
    fun orangeRibbonFilter(label: String? = null) = ColorRibbonFilter(label ?: this.name, Color(0xff, 0x76, 0, 0x99).toHexString())

    @JvmOverloads
    fun yellowRibbonFilter(label: String? = null) = ColorRibbonFilter(label ?: this.name, Color(0xff, 251, 0, 0x99).toHexString())

    @JvmOverloads
    fun redRibbonFilter(label: String? = null) = ColorRibbonFilter(label ?: this.name, Color(0xff, 0, 0, 0x99).toHexString())

    @JvmOverloads
    fun blueRibbonFilter(label: String? = null) = ColorRibbonFilter(label ?: this.name, Color(0, 0, 255, 0x99).toHexString())

    fun overlayFilter(fgFile: File) = OverlayFilter(fgFile)

    @JvmOverloads
    fun chromeLike(
        label: String? = null,
        ribbonColor: String? = null,
        labelColor: String? = null,
        fontName: String? = null,
        font: File? = null,
        labelPadding: Int? = null,
        overlayHeight: Float? = null,
        textSizeRatio: Float? = null,
        gravity: ChromeLikeFilter.Gravity? = null,
    ) = ChromeLikeFilter(
        label ?: this.name,
        ribbonColor = ribbonColor,
        labelColor = labelColor,
        labelPadding = labelPadding,
        gravity = gravity,
        fontName = fontName,
        fontResource = font,
        overlayHeight = overlayHeight,
        textSizeRatio = textSizeRatio,
    )

    fun chromeLike(properties: Map<String, Any>): ChromeLikeFilter {
        val gravity = properties["gravity"]?.toString()
            ?.uppercase()
            ?.let { ChromeLikeFilter.Gravity.valueOf(it) }
        val ribbonText = properties["label"]?.toString()
        val background = properties["ribbonColor"]?.toString()
        val labelColor = properties["labelColor"]?.toString()
        val labelPadding = properties["labelPadding"]?.toString()?.toDoubleOrNull()?.roundToInt()
        val overlayHeight = properties["overlayHeight"]?.toString()?.toFloatOrNull()
        val textSizeRatio = properties["textSizeRatio"]?.toString()?.toFloatOrNull()

        val fontName: String?
        val font: File?
        when (val fontRaw = properties["font"]) {
            is File -> {
                fontName = null
                font = fontRaw
            }

            is String -> {
                fontName = fontRaw
                font = null
            }

            else -> {
                fontName = properties["fontName"]?.toString()
                font = null
            }
        }

        return chromeLike(
            gravity = gravity,
            label = ribbonText,
            ribbonColor = background,
            labelColor = labelColor,
            labelPadding = labelPadding,
            fontName = fontName,
            font = font,
            overlayHeight = overlayHeight,
            textSizeRatio = textSizeRatio,
        )
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}

internal fun Color.toHexString() = "#%02x%02x%02x%02x".format(alpha, red, green, blue)

internal fun String.toColor(): Color {
    val value = java.lang.Long.decode(this)

    return when (length) {
        "#AARRGGBB".length -> {
            val alpha = (value shr 24 and 0xFF).toInt()
            val red = (value shr 16 and 0xFF).toInt()
            val green = (value shr 8 and 0xFF).toInt()
            val blue = (value and 0xFF).toInt()

            Color(red, green, blue, alpha)
        }

        "#RRGGBB".length -> {
            val red = (value shr 16 and 0xFF).toInt()
            val green = (value shr 8 and 0xFF).toInt()
            val blue = (value and 0xFF).toInt()
            Color(red, green, blue)
        }

        else -> Color.decode(this)
    }
}

private fun Iterable<*>?.toDrawingOptions() = this?.map { it.toString() }.orEmpty()
    .map { rawOption ->
        val option = ColorRibbonFilter.DrawingOption.values().firstOrNull { rawOption.matchesEnum(it) }
        checkNotNull(option) { "Unknown option: $rawOption. Use one of ${ColorRibbonFilter.DrawingOption.values().map { it.name }}" }
    }
    .toSet()

private fun <T : Enum<T>> String.matchesEnum(option: T) = replace("_", "").equals(option.name.replace("_", ""), ignoreCase = true)

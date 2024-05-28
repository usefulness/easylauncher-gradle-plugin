package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.plugin.models.IconFile
import com.project.starter.easylauncher.plugin.models.IconType
import com.project.starter.easylauncher.plugin.utils.vectorFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class XmlReaderTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `getLauncherIcon without 'roundIcon'`() {
        val manifest = tempDir.resolve("AndroidManifest.xml")
        manifest.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.akaita.android.easylauncher.example"
                android:versionCode="1"
                android:versionName="1.0" >
                <uses-sdk
                    android:minSdkVersion="15"
                    android:targetSdkVersion="23" />
                <application
                    android:allowBackup="true"
                    android:icon="@drawable/ic_launcher"
                    android:label="@string/app_name"
                    android:theme="@style/AppTheme" >
                </application>
            </manifest>
            """.trimIndent(),
        )

        val icon = manifest.getLauncherIcons()

        assertThat(icon).isEqualTo(mapOf("@drawable/ic_launcher" to IconType.Default))
    }

    @Test
    fun `getLauncherIcon without 'icon' nor 'roundIcon'`() {
        val manifest = tempDir.resolve("AndroidManifest.xml")
        manifest.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.akaita.android.easylauncher.example"
                android:versionCode="1"
                android:versionName="1.0" >
                <uses-sdk
                    android:minSdkVersion="15"
                    android:targetSdkVersion="23" />
                <application
                    android:allowBackup="true"
                    android:label="@string/app_name"
                    android:theme="@style/AppTheme" >
                </application>
            </manifest>
            """.trimIndent(),
        )

        val icon = manifest.getLauncherIcons()

        assertThat(icon).isEmpty()
    }

    @Test
    fun `getLauncherIcon with both 'icon' and 'roundIcon'`() {
        val manifest = tempDir.resolve("AndroidManifest.xml")
        manifest.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.akaita.android.easylauncher.example"
                android:versionCode="1"
                android:versionName="1.0" >
                <uses-sdk
                    android:minSdkVersion="15"
                    android:targetSdkVersion="23" />
                <application
                    android:allowBackup="true"
                    android:icon="@drawable/ic_launcher"
                    android:roundIcon="@drawable/ic_launcher_round"
                    android:label="@string/app_name"
                    android:theme="@style/AppTheme" >
                </application>
            </manifest>
            """.trimIndent(),
        )

        val icon = manifest.getLauncherIcons()

        assertThat(icon).isEqualTo(
            mapOf(
                "@drawable/ic_launcher" to IconType.Default,
                "@drawable/ic_launcher_round" to IconType.Round,
            ),
        )
    }

    @Test
    fun `getLauncherIcon with both 'icon' and 'roundIcon' set to the same value`() {
        val manifest = tempDir.resolve("AndroidManifest.xml")
        manifest.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.akaita.android.easylauncher.example"
                android:versionCode="1"
                android:versionName="1.0" >
                <uses-sdk
                    android:minSdkVersion="15"
                    android:targetSdkVersion="23" />
                <application
                    android:allowBackup="true"
                    android:icon="@drawable/ic_launcher"
                    android:roundIcon="@drawable/ic_launcher"
                    android:label="@string/app_name"
                    android:theme="@style/AppTheme" >
                </application>
            </manifest>
            """.trimIndent(),
        )

        val icon = manifest.getLauncherIcons()

        assertThat(icon).containsEntry("@drawable/ic_launcher", IconType.Round)
    }

    @Test
    fun `getLauncherIcon with manifest placeholders`() {
        val manifest = tempDir.resolve("AndroidManifest.xml")
        manifest.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.akaita.android.easylauncher.example"
                android:versionCode="1"
                android:versionName="1.0" >
                <uses-sdk
                    android:minSdkVersion="15"
                    android:targetSdkVersion="23" />
                <application
                    android:allowBackup="true"
                    android:icon="${"$"}{has_placeholder}"
                    android:roundIcon="${"$"}{does_not_have_placeholder}"
                    android:label="@drawable/ic_launcher"
                    android:theme="@style/AppTheme" >
                </application>
            </manifest>
            """.trimIndent(),
        )

        val icon = manifest.getLauncherIcons(mapOf("has_placeholder" to "@mipmap/ic_launcher"))

        assertThat(icon).isEqualTo(
            mapOf(
                "@mipmap/ic_launcher" to IconType.Default,
                "\${does_not_have_placeholder}" to IconType.Round,
            ),
        )
    }

    @Test
    fun `getLauncherIcon with manifest placeholders with replacement`() {
        val manifest = tempDir.resolve("AndroidManifest.xml")
        manifest.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.akaita.android.easylauncher.example"
                android:versionCode="1"
                android:versionName="1.0" >
                <uses-sdk
                    android:minSdkVersion="15"
                    android:targetSdkVersion="23" />
                <application
                    android:allowBackup="true"
                    android:icon="@drawable/${"$"}{cat_image}"
                    android:roundIcon="@mipmap/${"$"}{dog_image}"
                    android:label="@drawable/ic_launcher"
                    android:theme="@style/AppTheme" />
            </manifest>
            """.trimIndent(),
        )

        val icon = manifest.getLauncherIcons(mapOf("cat_image" to "cat", "dog_image" to "dog"))

        assertThat(icon).isEqualTo(
            mapOf(
                "@drawable/cat" to IconType.Default,
                "@mipmap/dog" to IconType.Round,
            ),
        )
    }

    @Test
    fun `parses adaptive icon`() {
        val adaptiveIcon = tempDir.resolve("ic_launcher.xml")
        adaptiveIcon.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
                <background android:drawable="@drawable/ic_launcher_background" />
                <foreground android:drawable="@mipmap/ic_launcher_foreground" />
            </adaptive-icon>

            """.trimIndent(),
        )

        val icon = adaptiveIcon.tryParseXmlIcon() as IconFile.Adaptive

        assertThat(icon.background).isEqualTo("@drawable/ic_launcher_background")
        assertThat(icon.foreground).isEqualTo("@mipmap/ic_launcher_foreground")
        assertThat(icon.file.path).isEqualTo(adaptiveIcon.path)
    }

    @Test
    fun `parses adaptive icon with monochrome option`() {
        val adaptiveIcon = tempDir.resolve("ic_launcher.xml")
        adaptiveIcon.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
                <background android:drawable="@drawable/ic_launcher_background" />
                <foreground android:drawable="@mipmap/ic_launcher_foreground" />
                <monochrome android:drawable="@mipmap/ic_launcher_foreground_monochrome" />
            </adaptive-icon>

            """.trimIndent(),
        )

        val icon = adaptiveIcon.tryParseXmlIcon() as IconFile.Adaptive

        assertThat(icon.background).isEqualTo("@drawable/ic_launcher_background")
        assertThat(icon.foreground).isEqualTo("@mipmap/ic_launcher_foreground")
        assertThat(icon.monochrome).isEqualTo("@mipmap/ic_launcher_foreground_monochrome")
        assertThat(icon.file.path).isEqualTo(adaptiveIcon.path)
    }

    @Test
    fun `parses drawable resource`() {
        val drawableResource = tempDir.resolve("ic_launcher.xml")
        drawableResource.writeText(vectorFile())

        val icon = drawableResource.tryParseXmlIcon()

        assertThat(icon).isEqualTo(
            IconFile.XmlDrawable.Vector(
                file = drawableResource,
                width = 24,
                height = 24,
            ),
        )
    }

    @Test
    fun `does not parse txt file as adaptive icon`() {
        val adaptiveIcon = tempDir.resolve("ic_launcher.txt")
        adaptiveIcon.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
                <background android:drawable="@drawable/ic_launcher_background" />
                <foreground android:drawable="@drawable/ic_launcher_foreground" />
            </adaptive-icon>

            """.trimIndent(),
        )

        val icon = adaptiveIcon.tryParseXmlIcon()

        assertThat(icon).isNull()
    }
}

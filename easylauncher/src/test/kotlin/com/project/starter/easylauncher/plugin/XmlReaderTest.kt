package com.project.starter.easylauncher.plugin

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
            """.trimIndent()
        )

        val icon = manifest.getLauncherIcons()

        assertThat(icon).containsExactly("@drawable/ic_launcher")
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
            """.trimIndent()
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
            """.trimIndent()
        )

        val icon = manifest.getLauncherIcons()

        assertThat(icon).containsExactly("@drawable/ic_launcher", "@drawable/ic_launcher_round")
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
            """.trimIndent()
        )

        val icon = manifest.getLauncherIcons()

        assertThat(icon).containsExactly("@drawable/ic_launcher")
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
            """.trimIndent()
        )

        val icon = manifest.getLauncherIcons(mapOf("has_placeholder" to "@mipmap/ic_launcher"))

        assertThat(icon).containsExactly("@mipmap/ic_launcher", "\${does_not_have_placeholder}")
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
            """.trimIndent()
        )

        val icon = manifest.getLauncherIcons(mapOf("cat_image" to "cat", "dog_image" to "dog"))

        assertThat(icon).containsExactly("@drawable/cat", "@mipmap/dog")
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

            """.trimIndent()
        )

        val icon = adaptiveIcon.asAdaptiveIcon()

        assertThat(icon?.background).isEqualTo("@drawable/ic_launcher_background")
        assertThat(icon?.foreground).isEqualTo("@mipmap/ic_launcher_foreground")
        assertThat(icon?.file?.path).isEqualTo(adaptiveIcon.path)
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

            """.trimIndent()
        )

        val icon = adaptiveIcon.asAdaptiveIcon()

        assertThat(icon).isNull()
    }
}

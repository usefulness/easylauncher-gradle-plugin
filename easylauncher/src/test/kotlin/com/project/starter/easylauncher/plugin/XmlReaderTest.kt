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
}

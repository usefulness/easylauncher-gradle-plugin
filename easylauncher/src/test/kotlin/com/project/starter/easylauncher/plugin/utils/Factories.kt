package com.project.starter.easylauncher.plugin.utils

import org.intellij.lang.annotations.Language
import java.io.File

fun File.buildScript(androidBlock: () -> String = { "" }, easylauncherBlock: () -> String = { "" }) {
    @Language("groovy")
    val buildScript =
        """
        plugins {
            id 'com.android.application'
            id 'com.starter.easylauncher' 
        }
        
        repositories {
            mavenCentral()
            google()
        }
        
        android {
            defaultConfig {
                compileSdkVersion 29
                minSdkVersion 23
            }
            ${androidBlock()}
        }
        
        easylauncher {
            ${easylauncherBlock()}
        }
        
        dependencies {
            testImplementation 'junit:junit:4.13'
        }
                    
        """.trimIndent()
    writeText(buildScript)
}

fun File.libraryBuildscript(androidBlock: () -> String = { "" }, easylauncherBlock: () -> String = { "" }) {
    @Language("groovy")
    val buildScript =
        """
        plugins {
            id 'com.android.library'
            id 'com.starter.easylauncher' 
        }
        
        repositories {
            mavenCentral()
            google()
            jcenter()
        }
        
        android {
            defaultConfig {
                compileSdkVersion 29
                minSdkVersion 23
            }
            ${androidBlock()}
        }
        
        easylauncher {
            ${easylauncherBlock()}
        }
        
        dependencies {
            testImplementation 'junit:junit:4.13'
        }
        
        """.trimIndent()
    writeText(buildScript)
}

internal fun vectorFile() =
    """
    <vector xmlns:android="http://schemas.android.com/apk/res/android"
        android:width="24dp"
        android:height="24dp"
        android:viewportWidth="24"
        android:viewportHeight="24">
        <path
            android:fillColor="@android:color/black"
            android:pathData="M9,16.2L4.8,12l-1.4,1.4L9,19 21,7l-1.4,-1.4L9,16.2z" />
    </vector>
    """.trimIndent()

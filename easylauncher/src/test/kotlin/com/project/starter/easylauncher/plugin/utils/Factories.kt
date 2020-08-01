package com.project.starter.easylauncher.plugin.utils

import org.intellij.lang.annotations.Language
import java.io.File

fun File.buildScript(androidBlock: () -> String, easylauncherBlock: () -> String = { "" }) {
    @Language("groovy")
    val buildScript =
        """
                    plugins {
                        id 'com.android.application'
                        id 'com.starter.easylauncher' 
                    }
                    
                    repositories {
                        jcenter()
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

package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.plugin.utils.WithGradleProjectTest
import com.project.starter.easylauncher.plugin.utils.buildScript
import com.project.starter.easylauncher.plugin.utils.libraryBuildscript
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class EasyLauncherConfigTest : WithGradleProjectTest() {

    @BeforeEach
    fun setUp() {
        rootDirectory.resolve("src/main/AndroidManifest.xml") {
            writeText(
                """
                    <manifest package="com.example.app" />
                
                """.trimIndent()
            )
        }
    }

    @Test
    fun `custom ribbon`() {
        rootDirectory.resolve("build.gradle").buildScript(
            androidBlock = {
                """
                    buildTypes {
                        debug { }
                        release { }
                    }
                """.trimIndent()
            },
            easylauncherBlock = {
                """
                    buildTypes {
                        debug {
                            filters customRibbon(
                                        label: "bitcoin", 
                                        ribbonColor: "#8A123456", 
                                        labelColor: "#654321", 
                                        font: "Arial-Black"
                                    )
                        }
                    }
                """.trimIndent()
            }
        )

        runTask("assembleDebug", "--stacktrace")
    }

    @Test
    fun `chrome like ribbon`() {
        rootDirectory.resolve("build.gradle").buildScript(
            androidBlock = {
                """
                    buildTypes {
                        debug { }
                        release { }
                    }
                """.trimIndent()
            },
            easylauncherBlock = {
                """
                    buildTypes {
                        debug {
                            filters chromeLike(
                                        label: "bitcoin", 
                                        ribbonColor: "#8A123456", 
                                        labelColor: "#654321", 
                                        font: "Tahoma"
                                    )
                        }
                    }
                """.trimIndent()
            }
        )

        runTask("assembleDebug", "--stacktrace")
    }

    @Test
    fun `library config`() {
        rootDirectory.resolve("build.gradle").libraryBuildscript(
            androidBlock = {
                """
                    buildTypes {
                        debug { }
                        release { }
                    }
                """.trimIndent()
            },
            easylauncherBlock = {
                """
                    buildTypes {
                        debug {
                            filters = redRibbonFilter()
                        }
                    }
                """.trimIndent()
            }
        )

        runTask("assembleDebug", "--stacktrace")
    }
}

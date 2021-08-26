package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.plugin.utils.WithGradleProjectTest
import com.project.starter.easylauncher.plugin.utils.buildScript
import com.project.starter.easylauncher.plugin.utils.libraryBuildscript
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.GraphicsEnvironment

internal class EasyLauncherConfigTest : WithGradleProjectTest() {

    private val fixtureFont = GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts.first()

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
            easylauncherBlock = {
                """
                    buildTypes {
                        debug {
                            filters customRibbon(
                                        label: "bitcoin", 
                                        ribbonColor: "#8A123456", 
                                        labelColor: "#654321", 
                                        font: "${fixtureFont.name}",
                                        drawingOptions: ["IgnoreTransparentPixels"],
                                    )
                        }
                    }
                """.trimIndent()
            }
        )

        runTask("assembleDebug")
    }

    @Test
    fun `custom ribbon - drawing options`() {
        rootDirectory.resolve("build.gradle").buildScript(
            easylauncherBlock = {
                """
                    buildTypes {
                        debug {
                            filters customRibbon(
                                        label: "bitcoin", 
                                        ribbonColor: "#8A123456", 
                                        labelColor: "#654321", 
                                        font: "${fixtureFont.name}",
                                        drawingOptions: ["IgnoreTransparentPixels", "AddExtraPadding"],
                                    )
                        }
                    }
                """.trimIndent()
            }
        )

        runTask("assembleDebug")

        rootDirectory.resolve("build.gradle").buildScript(
            easylauncherBlock = {
                """
                    buildTypes {
                        debug {
                            filters customRibbon(
                                        label: "bitcoin", 
                                        ribbonColor: "#8A123456", 
                                        labelColor: "#654321", 
                                        font: "${fixtureFont.name}",
                                        drawingOptions: ["AnUnknownOption"],
                                    )
                        }
                    }
                """.trimIndent()
            }
        )

        val result = runTask("assembleDebug", shouldFail = true)
        assertThat(result.output).contains("Unknown option: AnUnknownOption. Use one of [IGNORE_TRANSPARENT_PIXELS, ADD_EXTRA_PADDING]")
    }

    @Test
    fun `chrome like ribbon`() {
        rootDirectory.resolve("build.gradle").buildScript(
            easylauncherBlock = {
                """
                    buildTypes {
                        debug {
                            filters chromeLike(
                                        label: "bitcoin", 
                                        ribbonColor: "#8A123456", 
                                        labelColor: "#654321", 
                                        font: "${fixtureFont.name}",
                                        gravity: "top",
                                        labelPadding: 12,
                                        overlayHeight: 0.4,
                                        textSizeRatio: 0.2
                                    )
                        }
                    }
                """.trimIndent()
            }
        )

        runTask("assembleDebug")
    }

    @Test
    fun `library config`() {
        rootDirectory.resolve("build.gradle").libraryBuildscript(
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

        runTask("assembleDebug")
    }

    @Test
    fun `font error messages`() {
        rootDirectory.resolve("build.gradle").buildScript(
            androidBlock = { "" },
            easylauncherBlock = {
                """
                    buildTypes {
                        debug {
                            filters = chromeLike(font: "fixture-nonexistent-font")
                        }
                    }
                """.trimIndent()
            }
        )

        val nonExistentFontResult = runTask("assembleDebug")
        assertThat(nonExistentFontResult.task(":easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        rootDirectory.resolve("build.gradle").buildScript(
            androidBlock = { "" },
            easylauncherBlock = {
                """
                    buildTypes {
                        debug {
                            filters = chromeLike(font: file("invalid-file.ttf"))
                        }
                    }
                """.trimIndent()
            }
        )

        val invalidFontFile = runTask("assembleDebug", shouldFail = true)

        assertThat(invalidFontFile.output).contains("invalid-file.ttf does not exits. Make sure it points at existing font resource")
    }
}

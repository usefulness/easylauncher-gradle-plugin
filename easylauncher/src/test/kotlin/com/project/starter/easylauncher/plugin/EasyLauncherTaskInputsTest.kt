package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.plugin.utils.WithGradleProjectTest
import com.project.starter.easylauncher.plugin.utils.androidManifest
import com.project.starter.easylauncher.plugin.utils.buildScript
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

internal class EasyLauncherTaskInputsTest : WithGradleProjectTest() {

    @TempDir
    lateinit var buildCacheDir: File

    private lateinit var moduleRoot: File

    private val generatedIcon get() = moduleRoot.resolve("build/generated/res/easylauncherDebug/mipmap/ic_launcher.png")

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText(
                // language=groovy
                """
                buildCache {
                    local {
                        directory = new File("${buildCacheDir.invariantSeparatorsPath}")
                    }
                }
                include ":app"
                """.trimIndent(),
            )

            moduleRoot = resolve("app") {
                resolve("src/main/AndroidManifest.xml") { writeText(androidManifest()) }
                resolve("src/main/res/mipmap/ic_launcher.png") { writeSolidPng(Color.RED) }
                resolve("src/main/res/mipmap/ic_launcher_round.png") { writeSolidPng(Color.RED) }
                resolve("src/main/res/mipmap/ic_other.png") { writeSolidPng(Color.BLUE) }
                resolve("overlay.png") { writeSolidPng(Color.GREEN, width = 8, height = 2) }
                resolve("font.ttf") {
                    writeBytes(EasyLauncherTaskInputsTest::class.java.classLoader.getResource("Roboto-Regular.ttf")!!.readBytes())
                }
                resolve("build.gradle").buildScript(
                    easylauncherBlock = {
                        // language=groovy
                        """
                        buildTypes {
                            debug {
                                filters(
                                    customRibbon(label: "debug", font: file("font.ttf")),
                                    overlayFilter(file("overlay.png")),
                                )
                            }
                        }
                        """.trimIndent()
                    },
                )
            }
        }
    }

    @Test
    fun `stays up-to-date when nothing changes`() {
        assertThat(runTask("easylauncherDebug").task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        assertThat(runTask("easylauncherDebug").task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
    }

    @Test
    fun `reruns when source icon content changes at the same path`() {
        runTask("easylauncherDebug")
        assertThat(generatedIcon.cornerPixel()).isEqualTo(Color.RED.rgb)

        moduleRoot.resolve("src/main/res/mipmap/ic_launcher.png").writeSolidPng(Color.YELLOW)
        val result = runTask("easylauncherDebug")

        assertThat(result.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(generatedIcon.cornerPixel()).isEqualTo(Color.YELLOW.rgb)
    }

    @Test
    fun `reruns when manifest points at a different icon`() {
        runTask("easylauncherDebug")

        moduleRoot.resolve("src/main/AndroidManifest.xml").writeText(
            androidManifest().replace("@mipmap/ic_launcher\"", "@mipmap/ic_other\""),
        )
        val result = runTask("easylauncherDebug")

        assertThat(result.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(generatedIcon).doesNotExist()
        assertThat(moduleRoot.resolve("build/generated/res/easylauncherDebug/mipmap/ic_other.png").cornerPixel())
            .isEqualTo(Color.BLUE.rgb)
    }

    @Test
    fun `reruns when overlay file content changes`() {
        runTask("easylauncherDebug")
        val before = generatedIcon.readBytes()

        moduleRoot.resolve("overlay.png").writeSolidPng(Color.MAGENTA, width = 8, height = 2)
        val result = runTask("easylauncherDebug")

        assertThat(result.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(generatedIcon.readBytes()).isNotEqualTo(before)
    }

    @Test
    fun `reruns when font file content changes`() {
        runTask("easylauncherDebug")

        moduleRoot.resolve("font.ttf").appendBytes(byteArrayOf(0))
        val result = runTask("easylauncherDebug")

        assertThat(result.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `depends on tasks generating resource directories`() {
        moduleRoot.resolve("build.gradle").buildScript(
            androidBlock = {
                // language=groovy
                """
                buildFeatures {
                    resValues = true
                }
                defaultConfig {
                    resValue "string", "generated_value", "generated"
                }
                """.trimIndent()
            },
        )

        val result = runTask("easylauncherDebug")

        assertThat(result.task(":app:generateDebugResValues")?.outcome).isNotNull()
        assertThat(result.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `is loaded from build cache after project relocation`(@TempDir otherLocation: File) {
        assertThat(runTask("easylauncherDebug", "--build-cache").task(":app:easylauncherDebug")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)

        val relocated = otherLocation.resolve("relocated")
        rootDirectory.copyRecursively(relocated)
        relocated.resolve("app/build").deleteRecursively()
        val result = runTask("easylauncherDebug", "--build-cache", projectDir = relocated)

        assertThat(result.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.FROM_CACHE)
    }

    private fun File.writeSolidPng(color: Color, width: Int = 48, height: Int = width) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        image.createGraphics().apply {
            this.color = color
            fillRect(0, 0, width, height)
            dispose()
        }
        ImageIO.write(image, "png", this)
    }

    private fun File.cornerPixel() = ImageIO.read(this).let { it.getRGB(it.width - 1, it.height - 1) }
}

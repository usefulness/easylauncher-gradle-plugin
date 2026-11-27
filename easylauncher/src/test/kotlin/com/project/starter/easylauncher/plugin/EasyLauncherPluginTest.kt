package com.project.starter.easylauncher.plugin

import com.project.starter.easylauncher.plugin.utils.WithGradleProjectTest
import com.project.starter.easylauncher.plugin.utils.adaptiveIcon
import com.project.starter.easylauncher.plugin.utils.androidManifest
import com.project.starter.easylauncher.plugin.utils.buildScript
import com.project.starter.easylauncher.plugin.utils.vectorFile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

internal class EasyLauncherPluginTest : WithGradleProjectTest() {

    lateinit var moduleRoot: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":app" """)

            moduleRoot = resolve("app") {
                resolve("src/main/AndroidManifest.xml") { writeText(androidManifest()) }
                resolve("src/main/res/mipmap-v26/ic_launcher.xml") { writeText(adaptiveIcon()) }
                resolve("src/main/res/mipmap/ic_launcher.png") {
                    writeBytes(File("src/test/resources/beta.png").readBytes())
                }
                resolve("src/main/res/drawable/ic_foreground.xml") { writeText(vectorFile()) }
                resolve("src/main/res/mipmap/ic_launcher_round.png") {
                    writeBytes(File("src/test/resources/beta.png").readBytes())
                }
            }
        }
    }

    @Test
    fun `applies plugin with minimal setup`() {
        moduleRoot.resolve("build.gradle").buildScript(
            androidBlock = {
                """
                buildTypes {
                    debug { }
                    superType { }
                    release { }
                }
                flavorDimensions += "version"
                productFlavors {
                    demo { dimension "version" }
                    full { dimension "version" }
                }
                """.trimIndent()
            },
        )
        val result = runTask("assembleDemoDebug")

        assertThat(result.task(":app:easylauncherDemoDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `can disable warnings`() {
        moduleRoot.resolve("build.gradle").buildScript(
            androidBlock = {
                """
                buildTypes {
                    debug { }
                    superType { }
                    release { }
                }
                flavorDimensions += "version"
                productFlavors {
                    demo { dimension "version" }
                    full { dimension "version" }
                }
                """.trimIndent()
            },
            easylauncherBlock = {
                """
                showWarnings = false
                """.trimIndent()
            },
        )
        val result = runTask("assembleDemoDebug")

        assertThat(result.output).doesNotContain("[easylauncher]")
    }

    @Test
    fun `does not add task for non debuggable variants`() {
        moduleRoot.resolve("build.gradle").buildScript(
            androidBlock = {
                """
            buildTypes {
                debug { }
                superType { debuggable false }
                release { }
            }
            flavorDimensions += "version"
            productFlavors {
                demo { dimension "version" }
                full { dimension "version" }
            }
                """.trimIndent()
            },
        )
        val result = runTask("assembleDemoRelease", "assembleFullSuperType")

        assertThat(result.task(":app:easylauncherDemoRelease")).isNull()
        assertThat(result.task(":app:easylauncherFullSuperType")).isNull()
    }

    @Test
    fun `does not add task for non debuggable variants by default`() {
        moduleRoot.resolve("build.gradle").buildScript(
            androidBlock = {
                """
                buildTypes {
                    debug { }
                    superType { debuggable false }
                    release { }
                }
                """.trimIndent()
            },
            easylauncherBlock = {
                """
                buildTypes {
                    superType {
                        filters = customRibbon(
                            label: "Custom name",
                            ribbonColor: "#00ff00",
                            labelColor: "#ff00ff",
                            position: "top",
                            textSizeRatio: "0.7"
                        )              
                    }
                }
                """.trimIndent()
            },
        )
        val result = runTask("assembleSuperType")

        assertThat(result.task(":app:easylauncherSuperType")).isNotNull()
    }

    @Test
    fun `applies each filter type`() {
        moduleRoot.resolve("build.gradle").buildScript(
            androidBlock = { "" },
            easylauncherBlock = {
                """
                productFlavors {
                    debug {
                        filters(
                            grayRibbonFilter(),
                            greenRibbonFilter(),
                            orangeRibbonFilter(),
                            yellowRibbonFilter(),
                            redRibbonFilter(),
                            blueRibbonFilter(),
                            chromeLike()
                        )
                    }
                }
                """.trimIndent()
            },
        )

        val result = runTask("assembleDebug")

        assertThat(result.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `tasks are cacheable`() {
        moduleRoot.resolve("build.gradle").buildScript(
            androidBlock = { "" },
            easylauncherBlock = {
                """
                productFlavors {
                    debug {
                        filters(
                            grayRibbonFilter(),
                            greenRibbonFilter(),
                            orangeRibbonFilter(),
                            yellowRibbonFilter(),
                            redRibbonFilter(),
                            blueRibbonFilter(),
                            chromeLike()
                        )
                    }
                }
                """.trimIndent()
            },
        )
        val cleanRun = runTask("assembleDebug")
        assertThat(cleanRun.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        val secondRun = runTask("assembleDebug")
        assertThat(secondRun.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
    }

    @Test
    fun `plugin is compatible with configuration cache`() {
        moduleRoot.resolve("build.gradle").buildScript(
            androidBlock = { "" },
            easylauncherBlock = {
                """
                productFlavors {
                    debug {
                        filters(
                            grayRibbonFilter(),
                            greenRibbonFilter(),
                            orangeRibbonFilter(),
                            yellowRibbonFilter(),
                            redRibbonFilter(),
                            blueRibbonFilter(),
                            customRibbon(label: "second", ribbonColor: "#6600CC", labelColor: "#FFFFFF", position: "bottom"),
                            chromeLike(),
                            chromeLike(
                                label: "JP2", 
                                ribbonColor: "#723D46", 
                                labelColor: "#EEFFFEE", 
                                labelPadding: 25, 
                                overlayHeight: 0.6, 
                                textSizeRatio: 0.15
                            ),
                        )
                    }
                }
                """.trimIndent()
            },
        )

        val cleanRun = runTask("assembleDebug", "--configuration-cache")
        assertThat(cleanRun.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(cleanRun.output).contains("Calculating task graph as no cached configuration is available for tasks")

        val secondRun = runTask("assembleDebug", "--configuration-cache")
        assertThat(secondRun.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
        assertThat(secondRun.output).contains("Configuration cache entry reused")
    }

    @Test
    fun `plugin is compatible with isolated projects`() {
        moduleRoot.resolve("build.gradle").buildScript(
            androidBlock = { "" },
            easylauncherBlock = {
                """
                productFlavors {
                    debug {
                        filters(
                            grayRibbonFilter(),
                            greenRibbonFilter(),
                            orangeRibbonFilter(),
                            yellowRibbonFilter(),
                            redRibbonFilter(),
                            blueRibbonFilter(),
                            customRibbon(label: "second", ribbonColor: "#6600CC", labelColor: "#FFFFFF", position: "bottom"),
                            chromeLike(),
                            chromeLike(
                                label: "JP2", 
                                ribbonColor: "#723D46", 
                                labelColor: "#EEFFFEE", 
                                labelPadding: 25, 
                                overlayHeight: 0.6, 
                                textSizeRatio: 0.15
                            ),
                        )
                    }
                }
                """.trimIndent()
            },
        )

        val result = runTask("assembleDebug", "-Dorg.gradle.unsafe.isolated-projects=true")
        assertThat(result.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `draws legible label on raster monochrome layer`() {
        moduleRoot.resolve("src/main/res/mipmap-v26/ic_launcher.xml") {
            writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
                    <background android:drawable="@android:drawable/arrow_down_float" />
                    <foreground android:drawable="@drawable/ic_foreground" />
                    <monochrome android:drawable="@mipmap/ic_monochrome" />
                </adaptive-icon>
                """.trimIndent(),
            )
        }
        moduleRoot.resolve("src/main/res/mipmap-xxxhdpi/ic_monochrome.png") {
            ImageIO.write(BufferedImage(432, 432, BufferedImage.TYPE_INT_ARGB), "png", this)
        }
        moduleRoot.resolve("build.gradle").buildScript(
            easylauncherBlock = {
                """
                buildTypes {
                    debug {
                        filters(chromeLike(label: "debug", textSizeRatio: 0.2f))
                    }
                }
                """.trimIndent()
            },
        )

        runTask("easylauncherDebug")

        val outputRoot = moduleRoot.resolve("build/generated/res/easylauncherDebug")
        assertThat(outputRoot.resolve("mipmap-v26/ic_launcher.xml"))
            .content()
            .contains("""<monochrome android:drawable="@mipmap/ic_monochrome_easylauncher_monochrome" />""")
        val monochrome = ImageIO.read(outputRoot.resolve("mipmap-xxxhdpi/ic_monochrome_easylauncher_monochrome.png"))
        val ribbonAlphas = (300 until 432).flatMap { y -> (0 until 432).map { x -> monochrome.getRGB(x, y) ushr 24 } }
        assertSoftly { softly ->
            softly.assertThat(monochrome.getRGB(216, 431) ushr 24).isEqualTo(0xFF)
            softly.assertThat(ribbonAlphas).contains(0x00)
            softly.assertThat(monochrome.getRGB(216, 10) ushr 24).isEqualTo(0x00)
        }
    }

    @Test
    fun `generates separate monochrome layer when it shares a vector drawable with foreground`() {
        moduleRoot.resolve("src/main/res/mipmap-v26/ic_launcher.xml") {
            writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
                    <background android:drawable="@android:drawable/arrow_down_float" />
                    <foreground android:drawable="@drawable/ic_foreground" />
                    <monochrome android:drawable="@drawable/ic_foreground" />
                </adaptive-icon>
                """.trimIndent(),
            )
        }
        moduleRoot.resolve("build.gradle").buildScript()

        val result = runTask("assembleDebug")

        assertThat(result.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        val outputRoot = moduleRoot.resolve("build/generated/res/easylauncherDebug")
        assertSoftly { softly ->
            softly.assertThat(outputRoot.resolve("mipmap-v26/ic_launcher.xml"))
                .content()
                .contains("""<foreground android:drawable="@drawable/ic_foreground" />""")
                .contains("""<monochrome android:drawable="@drawable/ic_foreground_easylauncher_monochrome" />""")
            softly.assertThat(outputRoot.resolve("drawable-anydpi-v26/ic_foreground.xml")).exists()
            softly.assertThat(outputRoot.resolve("drawable-anydpi-v26/ic_foreground_easylauncher_monochrome.xml")).exists()
            softly.assertThat(outputRoot.resolve("drawable-xxxhdpi/overlay_ic_foreground_easylauncher_monochrome.png")).exists()
        }
    }

    @Test
    fun `generates proper tasks`() {
        moduleRoot.resolve("build.gradle").buildScript(
            androidBlock = {
                // language=groovy
                """
                 buildTypes {
                    debug {
                        //Debuggable, will get a default ribbon in the launcher icon
                    }
                    beta {
                        //Debuggable, will get a default ribbon in the launcher icon
                        debuggable true
                    }
                    canary {
                        //Non-debuggable, will not get any default ribbon
                        debuggable false
                    }
                    release {
                        //Non-debuggable, will not get any default ribbon
                    }
                }
                flavorDimensions += "xxx"
                productFlavors {
                    local { dimension "xxx" }
                    qa { dimension "xxx" }
                    staging { dimension "xxx" }
                    production { dimension "xxx" }
                }
                """.trimIndent()
            },
            easylauncherBlock = {
                // language=groovy
                """
                 productFlavors {
                    local {}
                    qa {
                        // Add one more filter to all `qa` variants
                        filters = redRibbonFilter()
                    }
                    staging {}
                    production {}
                }
                
                buildTypes {
                    beta {
                        // Add two more filters to all `beta` variants
                        filters = [
                                customColorRibbonFilter("#0000FF"),
                                overlayFilter(file("example-custom/launcherOverlay/beta.png"))
                        ]
                    }
                    canary {
                        // Remove ALL filters to `canary` variants
                        enable false
                    }
                    release {}
                }
                
                variants {
                    productionDebug {
                        // OVERRIDE all previous filters defined for `productionDebug` variant
                        filters = orangeRibbonFilter("custom")
                    }
                }
                """.trimIndent()
            },
        )
        val result = runTask("assemble")

        assertSoftly { softly ->
            listOf(
                "easylauncherLocalBeta",
                "easylauncherLocalDebug",
                "easylauncherProductionBeta",
                "easylauncherProductionDebug",
                "easylauncherQaBeta",
                "easylauncherQaDebug",
                "easylauncherQaRelease",
                "easylauncherStagingBeta",
                "easylauncherStagingDebug",
            ).forEach { taskName ->
                softly.assertThat(result.task(":app:$taskName")).isNotNull()
            }
        }
        assertSoftly { softly ->
            listOf(
                "easylauncherLocalCanary",
                "easylauncherLocalRelease",
                "easylauncherProductionCanary",
                "easylauncherProductionRelease",
                "easylauncherQaCanary",
                "easylauncherStagingCanary",
                "easylauncherStagingRelease",
            ).forEach { taskName ->
                softly.assertThat(result.task(":app:$taskName")).isNull()
            }
        }
    }
}

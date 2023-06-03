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
import java.io.File

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

        val cleanRun = runTask("assembleDebug", "--configuration-cache", skipJacoco = true)
        assertThat(cleanRun.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(cleanRun.output).contains("Calculating task graph as no configuration cache is available for tasks")

        val secondRun = runTask("assembleDebug", "--configuration-cache", skipJacoco = true)
        assertThat(secondRun.task(":app:easylauncherDebug")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
        assertThat(secondRun.output).contains("Configuration cache entry reused")
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

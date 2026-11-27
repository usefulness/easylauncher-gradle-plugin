package com.project.starter.easylauncher.plugin.utils

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File

@Suppress("UnnecessaryAbstractClass")
internal abstract class WithGradleProjectTest {

    @TempDir
    lateinit var rootDirectory: File

    protected fun runTask(vararg taskName: String, shouldFail: Boolean = false, projectDir: File = rootDirectory) =
        GradleRunner.create().apply {
            forwardOutput()
            withPluginClasspath()
            withArguments(*taskName)
            withProjectDir(projectDir)
        }.run {
            if (shouldFail) {
                buildAndFail()
            } else {
                build()
            }
        }

    protected fun File.resolve(relative: String, receiver: File.() -> Unit): File = resolve(relative).apply {
        parentFile.mkdirs()
        receiver()
    }
}

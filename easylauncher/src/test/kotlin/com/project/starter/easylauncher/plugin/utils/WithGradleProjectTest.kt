package com.project.starter.easylauncher.plugin.utils

import java.io.File
import java.io.InputStream
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir

@Suppress("UnnecessaryAbstractClass")
internal abstract class WithGradleProjectTest {

    @TempDir
    lateinit var rootDirectory: File

    protected fun runTask(vararg taskName: String, shouldFail: Boolean = false, skipJacoco: Boolean = false) = GradleRunner.create().apply {
        forwardOutput()
        withPluginClasspath()
        withArguments(*taskName)
        withProjectDir(rootDirectory)
        if (!skipJacoco) {
            withJaCoCo()
        }
    }.run {
        if (shouldFail) {
            buildAndFail()
        } else {
            build()
        }
    }

    private fun GradleRunner.withJaCoCo(): GradleRunner {
        javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")
            ?.toFile(File(projectDir, "gradle.properties"))
        return this
    }

    private fun InputStream.toFile(file: File) {
        use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
    }

    protected fun File.resolve(relative: String, receiver: File.() -> Unit): File = resolve(relative).apply {
        parentFile.mkdirs()
        receiver()
    }
}

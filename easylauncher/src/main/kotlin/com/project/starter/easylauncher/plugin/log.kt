package com.project.starter.easylauncher.plugin

import org.slf4j.LoggerFactory

@Suppress("ClassName")
internal object log {

    private const val TAG = "easylauncher"
    private val logger by lazy { LoggerFactory.getLogger(TAG) }

    fun warn(message: () -> String) {
        if (logger.isWarnEnabled) {
            logger.warn("[$TAG] ${message()}")
        }
    }

    fun info(message: () -> String) {
        if (logger.isInfoEnabled) {
            logger.info("[$TAG] ${message()}")
        }
    }
}

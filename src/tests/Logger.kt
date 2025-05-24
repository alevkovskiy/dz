package tests

import java.io.File
import java.util.logging.*

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import java.util.logging.Logger



object LoggerUtil {
    fun getLogger(name: String): Logger {
        val logger = Logger.getLogger(name)
        logger.useParentHandlers = false

        if (logger.handlers.isEmpty()) {
            val logsDir = File("logs")
            if (!logsDir.exists()) logsDir.mkdir()

            val handler = FileHandler("logs/$name.log", true)
            handler.formatter = SimpleFormatter()
            logger.addHandler(handler)
        }

        return logger
    }
}

abstract class LoggedTest {
    protected lateinit var logger: Logger

    abstract fun loggerName(): String

    @BeforeEach
    fun setupLogger(testInfo: TestInfo) {
        logger = tests.LoggerUtil.getLogger(loggerName())
        logger.info("\uD83D\uDD39 Начало теста: ${testInfo.displayName}")
    }

    @AfterEach
    fun logSuccess(testInfo: TestInfo) {
        logger.info("✅ Успешно завершён: ${testInfo.displayName}")
    }

    fun logWarning(message: String) {
        logger.warning("⚠️ $message")
    }

    fun logError(message: String) {
        logger.severe("❌ $message")
    }
}
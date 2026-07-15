package com.logger.loggerproject

import android.app.Application
import com.logger.logger_sdk.core.SdkConfig
import com.logger.logger_sdk.init.Logger
import com.logger.loggerproject.destination.ApiLogDestination

class LoggerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.initialize(
            application = this,
            config = SdkConfig(
                logDestination = ApiLogDestination(),
                batchSize = 20,
                timeoutMs = 30_000
            )
        )
    }
}
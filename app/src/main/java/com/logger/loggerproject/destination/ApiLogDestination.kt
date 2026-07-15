package com.logger.loggerproject.destination

import com.logger.logger_sdk.core.LogDestination
import com.logger.loggerproject.model.ScreenLog
import com.logger.loggerproject.serializer.ScreenLogSerializer

class ApiLogDestination : LogDestination<ScreenLog> {

    override val serializer = ScreenLogSerializer()

    override suspend fun log(
        logs: List<ScreenLog>
    ) {

        println("Uploading ${logs.size} logs")

        logs.forEach {
            println(it)
        }

        // Retrofit
        // Ktor
        // Firebase
        // Your backend
    }
}
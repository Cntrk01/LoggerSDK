package com.logger.loggerproject.serializer

import com.google.gson.Gson
import com.logger.logger_sdk.storage.LogSerializer
import com.logger.loggerproject.model.ScreenLog

class ScreenLogSerializer : LogSerializer<ScreenLog> {

    override fun serialize(value: ScreenLog): String {
        return Gson().toJson(value)
    }

    override fun deserialize(value: String): ScreenLog {
        return Gson().fromJson(
            value,
            ScreenLog::class.java
        )
    }
}
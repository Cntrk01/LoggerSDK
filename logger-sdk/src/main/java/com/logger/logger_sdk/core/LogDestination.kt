package com.logger.logger_sdk.core

import com.logger.logger_sdk.storage.LogSerializer

interface LogDestination<T> {
    val serializer: LogSerializer<T>
    suspend fun log(logs: List<T>)
}
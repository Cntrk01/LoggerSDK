package com.logger.logger_sdk.storage

internal interface OfflineLogRepository<T> {

    suspend fun insert(log: T)

    suspend fun insertAll(logs: List<T>)

    suspend fun getLogs(limit: Int): List<OfflineLog<T>>

    suspend fun delete(logs: List<Long>)
}
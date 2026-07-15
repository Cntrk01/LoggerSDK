package com.logger.logger_sdk.storage

import java.util.UUID

internal data class OfflineLog<T>(
    val id: Long,
    val uuid: String = UUID.randomUUID().toString(),
    val value: T,
    val createdAt: Long,
)
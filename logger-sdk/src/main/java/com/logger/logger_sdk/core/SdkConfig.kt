package com.logger.logger_sdk.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

data class SdkConfig<T>(
    val logDestination: LogDestination<T>,
    val onError: ((Throwable) -> Unit)? = null,
    // Batch
    val batchSize: Int = 20,
    val timeoutMs: Long = 30_000,

    // Channel
    val channelCapacity: Int = Channel.BUFFERED,
    val bufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,

    // Offline
    val maxOfflineFlushSize: Int = 50,
    val offlineSyncIntervalMs: Long = 1_000,
)
package com.logger.logger_sdk.lifecycle

import kotlinx.coroutines.flow.Flow

internal interface LifecycleObserver {
    fun observe(): Flow<LifecycleEvent>
}
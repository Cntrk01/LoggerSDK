package com.logger.logger_sdk.connectivity

import kotlinx.coroutines.flow.Flow

internal interface ConnectivityObserver {
    fun observe(): Flow<ConnectionState>
}
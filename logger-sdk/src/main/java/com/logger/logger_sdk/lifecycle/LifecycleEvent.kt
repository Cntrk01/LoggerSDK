package com.logger.logger_sdk.lifecycle

internal sealed interface LifecycleEvent {
    data object Foreground : LifecycleEvent
    data object Background : LifecycleEvent
    data object Destroy : LifecycleEvent
}
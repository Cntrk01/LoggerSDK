package com.logger.logger_sdk.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal class AndroidLifecycleObserver (
    val lifecycle: Lifecycle
) : LifecycleObserver{

    override fun observe(): Flow<LifecycleEvent> = callbackFlow {
        val callback = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                trySend(LifecycleEvent.Foreground)
            }
            override fun onStop(owner: LifecycleOwner) {
                trySend(LifecycleEvent.Background)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                trySend(LifecycleEvent.Destroy)
            }
        }

        lifecycle.addObserver(callback)

        awaitClose {
            lifecycle.removeObserver(callback)
        }
    }
}
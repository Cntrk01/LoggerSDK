package com.logger.logger_sdk.init

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.logger.logger_sdk.connectivity.AndroidConnectivityObserver
import com.logger.logger_sdk.connectivity.ConnectionState
import com.logger.logger_sdk.connectivity.ConnectivityObserver
import com.logger.logger_sdk.core.EngineCommand
import com.logger.logger_sdk.core.LoggerEngine
import com.logger.logger_sdk.core.SdkConfig
import com.logger.logger_sdk.lifecycle.AndroidLifecycleObserver
import com.logger.logger_sdk.lifecycle.LifecycleEvent
import com.logger.logger_sdk.storage.LoggerDatabase
import com.logger.logger_sdk.storage.RoomOfflineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

object Logger {

    private var initialized = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var loggerEngine: LoggerEngine<*>
    private lateinit var lifecycleObserver: AndroidLifecycleObserver
    private lateinit var connectivityObserver: ConnectivityObserver

    fun <T> initialize(
        application: Application,
        config: SdkConfig<T>,
    ) {
        check(!initialized) {
            "Logger SDK already initialized."
        }

        initialized = true

        val db = LoggerDatabase
            .getInstance(context = application)
            .logDao()

        val roomRepository = RoomOfflineRepository(
            dao = db,
            serializer = config.logDestination.serializer,
        )

        lifecycleObserver = AndroidLifecycleObserver(
            lifecycle = ProcessLifecycleOwner.get().lifecycle,
        )

        connectivityObserver = AndroidConnectivityObserver(
            context = application,
        )

        loggerEngine = LoggerEngine(
            config = config,
            roomRepository = roomRepository,
        )

        collectConnectivity()
        collectLifecycle()
    }

    fun sendLog(value: Any) {
        sendAction(action = EngineCommand.Log(value))
    }

    fun forceFlush(){
        sendAction(action = EngineCommand.ForceFlush)
    }

    fun shutDown(){
        sendAction(action = EngineCommand.Shutdown)
    }

    private fun sendAction(action: EngineCommand) {
        check(::loggerEngine.isInitialized) {
            "LoggerInit.initialize() must be called first."
        }

        scope.launch {
            loggerEngine.sendAction(action)
        }
    }

    private fun collectLifecycle() {
        scope.launch (Dispatchers.Main){
            lifecycleObserver
                .observe()
                .distinctUntilChanged()
                .collect {
                    when(it){
                        LifecycleEvent.Foreground -> {
                            //sendAction(EngineCommand.NetworkAvailable)
                        }
                        LifecycleEvent.Background -> {
                            sendAction(EngineCommand.ForceFlush)
                        }
                        LifecycleEvent.Destroy -> {
                            sendAction(EngineCommand.Shutdown)
                            scope.cancel()
                        }
                    }
                }
        }
    }

    private fun collectConnectivity() {
        scope.launch {
            connectivityObserver
                .observe()
                .distinctUntilChanged()
                .collect {
                    when(it){
                        ConnectionState.AVAILABLE -> {
                            sendAction(EngineCommand.NetworkAvailable)
                        }
                        ConnectionState.LOSING -> {
                            //sendAction(EngineCommand.NetworkLost)
                        }
                        ConnectionState.LOST -> {
                            sendAction(EngineCommand.NetworkLost)
                        }
                    }
                }
        }
    }
}
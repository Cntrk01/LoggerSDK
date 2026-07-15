package com.logger.logger_sdk.core

import com.logger.logger_sdk.storage.OfflineLogRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlin.coroutines.cancellation.CancellationException

internal class LoggerEngine<T>(
    private val config: SdkConfig<T>,
    private val roomRepository: OfflineLogRepository<T>,
) {
    private var isOnline: Boolean? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        config.onError?.invoke(throwable)
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    private val channel = Channel<EngineCommand>(
        capacity = config.channelCapacity,
        onBufferOverflow = config.bufferOverflow,
    )

    private var flushTimerJob: Job? = null

    init {
        require(config.batchSize > 0)
        require(config.timeoutMs > 0)
        require(config.channelCapacity > 0)

        startCollector()
    }

    //Public ifade de bunların önceki methodu olan EngineCommand typelarını vereceğim içte Logger.sendLog,sendForceFlush gibi.
    internal suspend fun sendAction(action: EngineCommand) {
        channel.send(action)
    }

    /**
     * Channel'dan sürekli veri tüketir.
     * Tek coroutine çalışır.
     */
    private fun startCollector() {
        scope.launch {
            val logList = mutableListOf<T>()

            while (isActive) {
                //select her döngüde tekrar çalışır mesela onReceive oldu bitti.Yeniden gelince tekrar çalışır sıfırlar
                //bundan dolayı timeout olayı burda patlar gelen süre dolmadan tekrar başlar.Ama biz öyle olmasını istemiyoruz.
                // Oyüzden kendi timeout sayacımı yazdım.
                select {
                    channel.onReceive { command: EngineCommand ->
                        when (command) {
                            is EngineCommand.Log -> {
                                if (logList.isEmpty()) {
                                    startFlushTimer()
                                }

                                logList += command.value as T

                                if (logList.size >= config.batchSize) {
                                    flush(buffer = logList)
                                }
                            }

                            EngineCommand.ForceFlush -> {
                                flush(buffer = logList)
                            }

                            EngineCommand.Shutdown -> {
                                flush(buffer = logList)
                                channel.close()
                                scope.cancel()
                            }

                            //TODO : İnternet geldiğinde de roomdan okuyup verileri göndermeliyiz.Sonrasında roomdan silmeliyiz.
                            // Bunun için roomdan okuduktan sonra callbackimi doğrudan tetikliyorum channelimi hiç doldurmuyorum.
                            is EngineCommand.NetworkAvailable -> {
                                isOnline = true
                                flushOfflineLogs()
                                flush(logList)
                            }

                            is EngineCommand.NetworkLost -> {
                                //TODO : İnternet gittiğinde channeldaki verileri aslında bufferdaki verileri okuyup rooma gömeceğiz
                                // 1. RAM'deki bekleyen logları Room'a taşı.
                                // 2. Bundan sonra gelecek loglar doğrudan Room'a yazılacak.
                                //flush(logList)
                                isOnline = false

                                if (logList.isNotEmpty()) {
                                    roomRepository.insertAll(logList)
                                    logList.clear()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun flush(
        buffer: MutableList<T>
    ) {
        if (buffer.isEmpty()) return

        stopFlushTimer()

        if (isOnline == true) {
            flushMemoryLogs(buffer)
        } else {
            roomRepository.insertAll(buffer)
            buffer.clear()
        }
    }

    /**
     * Batch gönder.
     */
    private suspend fun flushMemoryLogs(
        buffer: MutableList<T>
    ) {
        if (buffer.isEmpty()) return
        try {
            config.logDestination.log(buffer.toList())
            buffer.clear()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            config.onError?.invoke(e)
        }
    }

    private suspend fun flushOfflineLogs() {
        while (scope.isActive) {
            try {
                val logs = roomRepository.getLogs(config.maxOfflineFlushSize)
                if (logs.isEmpty()) break

                config.logDestination.log(
                    logs.map { it.value }
                )
                roomRepository.delete(
                    logs.map { it.id }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                config.onError?.invoke(e)
            }

            delay(config.offlineSyncIntervalMs)
        }
    }

    private fun startFlushTimer() {
        if (flushTimerJob?.isActive == true) return
        flushTimerJob = scope.launch {
            delay(config.timeoutMs)
            channel.send(EngineCommand.ForceFlush)
        }
    }

    //TODO : İnternet gittiğinde timer nasıl davranmalı ??
    private fun stopFlushTimer() {
        flushTimerJob?.cancel()
        flushTimerJob = null
    }
}
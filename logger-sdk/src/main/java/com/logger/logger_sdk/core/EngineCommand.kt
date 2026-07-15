package com.logger.logger_sdk.core

internal sealed interface EngineCommand {

    data class Log(
        val value: Any
    ) : EngineCommand

    data object ForceFlush : EngineCommand

    data object Shutdown : EngineCommand

    data object NetworkAvailable : EngineCommand

    data object NetworkLost : EngineCommand
}
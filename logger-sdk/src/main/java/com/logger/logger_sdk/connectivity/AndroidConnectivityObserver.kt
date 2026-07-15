package com.logger.logger_sdk.connectivity

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal class AndroidConnectivityObserver(
    context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(
            ConnectivityManager::class.java
        )

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun observe(): Flow<ConnectionState> = callbackFlow {
        trySend(currentConnectionState())

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                trySend(ConnectionState.AVAILABLE)
            }

            override fun onLosing(
                network: Network,
                maxMsToLive: Int
            ) {
                trySend(ConnectionState.LOSING)
            }

            override fun onLost(network: Network) {
                trySend(ConnectionState.LOST)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    private fun currentConnectionState(): ConnectionState {

        val network = connectivityManager.activeNetwork
            ?: return ConnectionState.LOST

        val capabilities =
            connectivityManager.getNetworkCapabilities(network)
                ?: return ConnectionState.LOST

        val hasInternet =
            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )

        val isValidated =
            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )

        return if (hasInternet && isValidated) {
            ConnectionState.AVAILABLE
        } else {
            ConnectionState.LOST
        }
    }
}
package com.logger.logger_sdk.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

//Bunu kolayca önlemek için observer'ı kullanan tarafta:
//
//connectivityObserver
//    .observe()
//    .distinctUntilChanged()
//    .collect { ... }
//
//kullanabilirsin. Böylece Available → Available → Available gibi tekrarlar filtrelenir ve engine gereksiz yere aynı komutu işlemez.
// Bu, özellikle farklı üreticilerin Android sürümlerindeki callback davranışlarına karşı güzel bir koruma sağlar.
internal class AndroidConnectivityObserver(
    context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(
            ConnectivityManager::class.java
        )

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
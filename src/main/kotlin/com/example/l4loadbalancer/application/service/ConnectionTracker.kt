package com.example.l4loadbalancer.application.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.InetSocketAddress

object ConnectionTracker {

    private val mutex = Mutex()
    private val activeConnections = mutableMapOf<InetSocketAddress, Int>()

    //Initialize the backends with zero active connection
    fun initialize(backendAddresses: List<InetSocketAddress>) {
        activeConnections.clear()
        backendAddresses.forEach { activeConnections[it] = 0 }
    }

    suspend fun incrementConnection(address: InetSocketAddress) {
        mutex.withLock {
            activeConnections[address] = activeConnections.getOrDefault(address, 0) + 1
        }
    }

    suspend fun decrementConnection(address: InetSocketAddress) {
        mutex.withLock {
            activeConnections[address] = maxOf(
                0,
                activeConnections.getOrDefault(address, 0) - 1
            )
        }
    }

    suspend fun getLeastActiveBackend(): InetSocketAddress {
        return mutex.withLock {
            activeConnections.minBy { it.value }.key
        }
    }

    fun clear() {
        activeConnections.clear()
    }
}
package com.example.l4loadbalancer.application.lbstrategy

import com.example.l4loadbalancer.application.service.BackendConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.InetSocketAddress

class RoundRobinStrategy(backendConfig: BackendConfig) : LBStrategy {
    private var currentBackEndIndex: Int = -1
    private val backends = backendConfig.backendAddresses
    private val mutex = Mutex()

    override suspend fun selectBackend(): InetSocketAddress {
        return mutex.withLock {
            currentBackEndIndex = (currentBackEndIndex + 1) % backends.size
            backends[currentBackEndIndex]
        }
    }
}
package com.example.l4loadbalancer.application.lbstrategy

import com.example.l4loadbalancer.application.service.BackendConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.InetSocketAddress

class WeightedRoundRobinStrategy(backendConfig: BackendConfig) : LBStrategy {

    private val backends = backendConfig.backendAddresses
    private val weights = backendConfig.weightsByBackendIndex
    private var currentIndex = -1
    private var remainingWeight = 0
    private val mutex = Mutex()

    override suspend fun selectBackend(): InetSocketAddress {
        return mutex.withLock {
            //If remainingWeight is 0, then move to the next backend
            if (remainingWeight == 0) {
                currentIndex = (currentIndex + 1) % backends.size
                remainingWeight = weights.getOrDefault(currentIndex, 1)
            }
            // Decrement the weight and return the current backend
            remainingWeight--
            backends[currentIndex]
        }

    }

}
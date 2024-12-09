package com.example.l4loadbalancer.application.lbstrategy

import com.example.l4loadbalancer.application.service.ConnectionTracker
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.InetSocketAddress

class LeastActiveConnectionStrategy : LBStrategy {
    private val mutex = Mutex()

    override suspend fun selectBackend(): InetSocketAddress {
        return mutex.withLock {
            //Find the backend with the least active connections
            ConnectionTracker.getLeastActiveBackend()
        }
    }
}
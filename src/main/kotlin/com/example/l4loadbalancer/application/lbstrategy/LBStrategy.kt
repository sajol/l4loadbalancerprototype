package com.example.l4loadbalancer.application.lbstrategy

import java.net.InetSocketAddress

interface LBStrategy {
    suspend fun selectBackend(): InetSocketAddress
}
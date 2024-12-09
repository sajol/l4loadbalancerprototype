package com.example.l4loadbalancer.application.service

import com.example.l4loadbalancer.application.lbstrategy.LBStrategy
import com.example.l4loadbalancer.application.lbstrategy.LeastActiveConnectionStrategy
import com.example.l4loadbalancer.application.lbstrategy.RoundRobinStrategy
import com.example.l4loadbalancer.application.lbstrategy.WeightedRoundRobinStrategy
import mu.KotlinLogging
import java.net.InetSocketAddress

class BackendConfigService {

    private val logger = KotlinLogging.logger {}
    private var backEndConfig: BackendConfig

    // Static data to simulate the behavior (will be fetched from DB in the future)
    private val backends = listOf(
        InetSocketAddress("localhost", 8081),
        InetSocketAddress("localhost", 8082),
        InetSocketAddress("localhost", 8083),
        InetSocketAddress("localhost", 8084),
    )
    private val backendWeights = mapOf(
        0 to 2,
        1 to 2,
        2 to 1,
        3 to 2,
    )
    private val roundRobinBackendConfig =
        BackendConfig(LbStrategyType.RoundRobin, backends, emptyMap())
    private val weightedRoundRobinBackendConfig =
        BackendConfig(LbStrategyType.WeightedRoundRobin, backends, backendWeights)
    private val leastActiveConnectionBackendConfig =
        BackendConfig(LbStrategyType.LeastActiveConnection, backends, emptyMap())

    // Map to hold strategy instances
    private val strategyInstances: MutableMap<LbStrategyType, LBStrategy> = mutableMapOf()

    init {
        // Initialize strategies
        strategyInstances[LbStrategyType.RoundRobin] = RoundRobinStrategy(roundRobinBackendConfig)
        strategyInstances[LbStrategyType.WeightedRoundRobin] =
            WeightedRoundRobinStrategy(weightedRoundRobinBackendConfig)
        strategyInstances[LbStrategyType.LeastActiveConnection] = LeastActiveConnectionStrategy()

        backEndConfig = getBackendConfig()
    }

    private fun getBackendConfig(): BackendConfig {
        return roundRobinBackendConfig // Replace with dynamic logic if needed
    }

    suspend fun selectBackend(): InetSocketAddress {
        val strategy = strategyInstances[backEndConfig.lbStrategyType]
            ?: throw IllegalStateException("No strategy found for ${backEndConfig.lbStrategyType}")
        val backend = strategy.selectBackend()
        logger.info { "Backend selected: ${backend.port}" }
        return backend
    }

    fun getBackendAddresses(): List<InetSocketAddress> {
        return backEndConfig.backendAddresses
    }

}


enum class LbStrategyType {
    WeightedRoundRobin,
    RoundRobin,
    LeastActiveConnection,
}

data class BackendConfig(
    val lbStrategyType: LbStrategyType,
    val backendAddresses: List<InetSocketAddress>,
    val weightsByBackendIndex: Map<Int, Int>
)
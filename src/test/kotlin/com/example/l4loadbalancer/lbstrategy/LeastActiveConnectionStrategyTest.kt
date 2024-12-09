package com.example.l4loadbalancer.lbstrategy

import com.example.l4loadbalancer.application.lbstrategy.LeastActiveConnectionStrategy
import com.example.l4loadbalancer.application.service.ConnectionTracker
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.net.InetSocketAddress

class LeastActiveConnectionStrategyTest : StringSpec({

    lateinit var backends: List<InetSocketAddress>
    lateinit var leastActiveConnectionStrategy: LeastActiveConnectionStrategy

    beforeEach {
        backends = listOf(
            InetSocketAddress("localhost", 8081),
            InetSocketAddress("localhost", 8082),
            InetSocketAddress("localhost", 8083)
        )
        ConnectionTracker.initialize(backends)
        leastActiveConnectionStrategy = LeastActiveConnectionStrategy()
    }

    afterEach {
        ConnectionTracker.clear()
    }


    "should select backend with least active connections" {
        // Setup backends
        ConnectionTracker.incrementConnection(backends[1])
        ConnectionTracker.incrementConnection(backends[2])

        // Select backend (should pick the least active: 8081)
        val selectedBackend = leastActiveConnectionStrategy.selectBackend()
        selectedBackend shouldBe backends[0]
    }

    "should handle concurrent connections and maintain least active connection logic" {
        // Simulate multiple concurrent requests
        val results = (1..10).map {
            async(Dispatchers.IO) {
                val selectedBackend = leastActiveConnectionStrategy.selectBackend()

                // Simulate connection establishment and release
                ConnectionTracker.incrementConnection(selectedBackend)
                delay(200L) // Simulate some processing time
                ConnectionTracker.decrementConnection(selectedBackend)

                selectedBackend
            }
        }.map { it.await() }

        // Verify backend utilization
        val groupedResults = results.groupingBy { it }.eachCount()

        assertSoftly {
            // Ensure all backends are utilized
            backends.forEach { backend ->
                groupedResults[backend] shouldNotBe null
            }

            // Check fairness (difference between max and min usage should be small)
            val maxConnections = groupedResults.values.maxOrNull()!!
            val minConnections = groupedResults.values.minOrNull()!!
            (maxConnections - minConnections) shouldBeLessThanOrEqual 2
        }
    }
})
package com.example.l4loadbalancer.config

import com.example.l4loadbalancer.application.service.BackendConfigService
import com.example.l4loadbalancer.application.service.L4LoadBalancerService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun backendConfigService(): BackendConfigService {
        return BackendConfigService()
    }

    @Bean
    fun loadBalancerService(backendConfigService: BackendConfigService): L4LoadBalancerService {
        return L4LoadBalancerService(backendConfigService)
    }
}
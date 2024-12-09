package com.example.l4loadbalancer

import com.example.l4loadbalancer.application.service.L4LoadBalancerService
import com.example.l4loadbalancer.config.AppConfig
import org.springframework.context.annotation.AnnotationConfigApplicationContext

fun main() {
    val context = AnnotationConfigApplicationContext(AppConfig::class.java)
    val loadBalancerService = context.getBean(L4LoadBalancerService::class.java)

    loadBalancerService.startServer()
}
package com.geovannycode.wallet.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaErrorConfig {

    @Bean
    fun errorHandler(kafkaTemplate: KafkaTemplate<Any, Any>): DefaultErrorHandler {
        // Publica a "wallet.movements.DLT" tras agotar los reintentos.
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate)
        // 3 reintentos, esperando 1 segundo entre cada uno.
        val backOff = FixedBackOff(1000L, 3L)
        return DefaultErrorHandler(recoverer, backOff)
    }
}
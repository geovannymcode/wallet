package com.geovannycode.wallet.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaErrorConfig {

    // Spring Boot 4 con Jackson 3 no autoconfigura este ObjectMapper clásico (Jackson 2);
    // lo necesitamos para serializar el payload del outbox (TransferService) y el
    // JsonSerializer de spring-kafka, que aún usan Jackson 2.
    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().findAndRegisterModules()

    @Bean
    fun errorHandler(kafkaTemplate: KafkaTemplate<Any, Any>): DefaultErrorHandler {
        // Publica a "wallet.movements.DLT" tras agotar los reintentos.
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate)
        // 3 reintentos, esperando 1 segundo entre cada uno.
        val backOff = FixedBackOff(1000L, 3L)
        return DefaultErrorHandler(recoverer, backOff)
    }
}
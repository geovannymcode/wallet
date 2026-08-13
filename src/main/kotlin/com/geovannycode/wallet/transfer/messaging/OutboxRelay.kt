package com.geovannycode.wallet.transfer.messaging

import jakarta.transaction.Transactional
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OutboxRelay(
    private val outbox: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    @Scheduled(fixedDelay = 2000)
    @Transactional
    fun publicarPendientes() {
        outbox.findBySentAtIsNullOrderByCreatedAt().forEach { fila ->
            kafkaTemplate.send(fila.topic, fila.msgKey, fila.payload)
            fila.sentAt = Instant.now()
        }
    }
}
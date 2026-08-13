package com.geovannycode.wallet.notification.messaging

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class DeadLetterListener {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["wallet.movements.DLT"], groupId = "dlt-demo")
    fun onDead(record: ConsumerRecord<String, String>) {
        log.warn("💀 Mensaje muerto en el DLT: key={} value={}", record.key(), record.value())
    }
}
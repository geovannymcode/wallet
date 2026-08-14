package com.geovannycode.wallet.transfer.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OutboxRelay(
    private val outbox: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val mapper: ObjectMapper
) {

    @Scheduled(fixedDelay = 2000)
    @Transactional
    fun publicarPendientes() {
        outbox.findBySentAtIsNullOrderByCreatedAt().forEach { fila ->
            // fila.payload ya es JSON en texto (TransferService lo serializó así para
            // poder guardarlo/inspeccionarlo en la tabla outbox). El KafkaTemplate usa
            // JsonSerializer (para publicar objetos del dominio): si le pasáramos ese
            // String tal cual, Jackson lo volvería a serializar como si fuera un valor
            // JSON y quedaría doblemente codificado. Lo parseamos de vuelta a un árbol
            // JSON para que se codifique una sola vez, con la forma correcta.
            kafkaTemplate.send(fila.topic, fila.msgKey, mapper.readTree(fila.payload))
            fila.sentAt = Instant.now()
        }
    }
}
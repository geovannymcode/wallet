package com.geovannycode.wallet.transfer.messaging

import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class MovimientoPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    fun publish(evento: MovimientoRegistrado) {
        // La clave es la cuenta de origen: los eventos de una misma cuenta
        // mantienen el orden entre ellos.
        kafkaTemplate.send(TOPIC, evento.fromId.toString(), evento)
    }

    companion object {
        const val TOPIC = "wallet.movements"
    }
}
package com.geovannycode.wallet.movement.messaging

import com.geovannycode.wallet.movement.domain.MovementService
import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class MovimientoPersistenceListener(
    private val movementService: MovementService
) {

    @KafkaListener(topics = ["wallet.movements"], groupId = "movement")
    fun onMovimiento(evento: MovimientoRegistrado) {
        movementService.record(evento.fromId, evento.toId, evento.amount)
    }
}
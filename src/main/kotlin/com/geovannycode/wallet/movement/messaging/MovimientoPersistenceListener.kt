package com.geovannycode.wallet.movement.messaging

import com.geovannycode.wallet.movement.domain.MovementService
import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import jakarta.transaction.Transactional
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class MovimientoPersistenceListener(
    private val movementService: MovementService,
    private val processedRepo: ProcessedEventRepository
) {

    @KafkaListener(topics = ["wallet.movements"], groupId = "movement")
    @Transactional
    fun onMovimiento(evento: MovimientoRegistrado) {
        if (processedRepo.existsById(evento.eventId)) {
            return // ya lo procesé, lo ignoro
        }
        movementService.record(evento.fromId, evento.toId, evento.amount)
        processedRepo.save(ProcessedEvent(evento.eventId))
    }
}
package com.geovannycode.wallet.movement.messaging

import com.geovannycode.wallet.movement.domain.MovementService
import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class MovimientoPersistenceListener(
    private val movementService: MovementService,
    private val processedRepo: ProcessedEventRepository,
    // Centinela para demostrar la DLQ en el taller: desactivado por defecto.
    // Activar solo para la demo con -Dwallet.demo.dlq-sentinel-enabled=true
    // (o wallet.demo.dlq-sentinel-enabled=true en application-local.yaml).
    @Value("\${wallet.demo.dlq-sentinel-enabled:false}") private val dlqSentinelEnabled: Boolean
) {

    @KafkaListener(topics = ["wallet.movements"], groupId = "movement")
    @Transactional
    fun onMovimiento(evento: MovimientoRegistrado) {
        if (dlqSentinelEnabled && evento.amount < BigDecimal.ZERO) {
            throw IllegalStateException("evento envenenado para la demo")
        }
        if (processedRepo.existsById(evento.eventId)) {
            return // ya lo procesé, lo ignoro
        }
        movementService.record(evento.fromId, evento.toId, evento.amount)
        processedRepo.save(ProcessedEvent(evento.eventId))
    }
}
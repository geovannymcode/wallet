package com.geovannycode.wallet.notification.messaging

import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import com.geovannycode.wallet.notification.domain.EmailNotifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class MovimientoNotificationListener(
    private val emailNotifier: EmailNotifier
) {

    @KafkaListener(topics = ["wallet.movements"], groupId = "notification")
    fun onMovimiento(evento: MovimientoRegistrado) {
        emailNotifier.notificar(evento)
    }
}
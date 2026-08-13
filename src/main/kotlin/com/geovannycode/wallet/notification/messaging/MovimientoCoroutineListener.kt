package com.geovannycode.wallet.notification.messaging

import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import com.geovannycode.wallet.notification.domain.NotificationService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Fase 9 (coroutines, opcional): consumidor no bloqueante que llama en paralelo
 * a 3 servicios externos (correo, push, antifraude) usando coroutineScope + async.
 *
 * Usa un groupId propio ("notification-coroutines-demo") para no interferir con
 * MovimientoNotificationListener (el que envía el correo real vía SMTP/Mailpit):
 * ambos consumen el mismo tópico "wallet.movements" de forma independiente, cada
 * uno con su propio offset, porque pertenecen a grupos de consumidores distintos.
 */
@Component
class MovimientoCoroutineListener(
    private val notificationService: NotificationService
) {

    @KafkaListener(topics = ["wallet.movements"], groupId = "notification-coroutines-demo")
    suspend fun onMovimiento(evento: MovimientoRegistrado) {
        notificationService.notificar(evento)
    }
}

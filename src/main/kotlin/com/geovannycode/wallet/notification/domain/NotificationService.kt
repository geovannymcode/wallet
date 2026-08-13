package com.geovannycode.wallet.notification.domain

import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val email: EmailClient,
    private val push: PushClient,
    private val antifraud: AntifraudClient
) {
    suspend fun notificar(evento: MovimientoRegistrado) = coroutineScope {
        val emailJob     = async { email.enviar(evento) }
        val pushJob      = async { push.enviar(evento) }
        val antifraudJob = async { antifraud.revisar(evento) }
        awaitAll(emailJob, pushJob, antifraudJob)
    }
}
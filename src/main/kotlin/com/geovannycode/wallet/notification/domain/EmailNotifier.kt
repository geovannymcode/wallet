package com.geovannycode.wallet.notification.domain

import com.geovannycode.wallet.account.domain.AccountService
import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EmailNotifier(
    private val accountService: AccountService,
    private val mailPort: MailPort,
    @Value("\${wallet.mail.from}") private val mailFrom: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun notificar(evento: MovimientoRegistrado) {
        val destino = accountService.getById(evento.toId)   // la cuenta destino, con su email
        // La notificación es un efecto secundario: que falle el correo no debe
        // tumbar el consumo del evento (el movimiento ya quedó registrado).
        runCatching {
            mailPort.send(
                from = mailFrom,
                to = destino.email,
                subject = "Recibiste un movimiento en tu wallet",
                body = "Hola ${destino.owner}, recibiste ${evento.amount} en tu cuenta."
            )
        }.onFailure { log.error("No se pudo enviar el correo de notificación", it) }
    }
}
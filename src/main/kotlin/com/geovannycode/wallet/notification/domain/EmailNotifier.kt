package com.geovannycode.wallet.notification.domain

import com.geovannycode.wallet.account.domain.AccountService
import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class EmailNotifier(
    private val accountService: AccountService,
    private val mailSender: JavaMailSender,
    @Value("\${wallet.mail.from}") private val mailFrom: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun notificar(evento: MovimientoRegistrado) {
        val destino = accountService.getById(evento.toId)   // la cuenta destino, con su email
        val mensaje = SimpleMailMessage().apply {
            from = mailFrom
            setTo(destino.email)
            subject = "Recibiste un movimiento en tu wallet"
            text = "Hola ${destino.owner}, recibiste ${evento.amount} en tu cuenta."
        }
        // La notificación es un efecto secundario: que falle el correo no debe
        // tumbar el consumo del evento (el movimiento ya quedó registrado).
        runCatching { mailSender.send(mensaje) }
            .onFailure { log.error("No se pudo enviar el correo de notificación", it) }
    }
}
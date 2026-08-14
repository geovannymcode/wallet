package com.geovannycode.wallet.notification.domain

import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

// Solo para el perfil "local": envía por SMTP a Mailpit (docker-compose), sin
// autenticación ni TLS. En producción se usa ResendMailPort (ver por qué en
// docs/LOCAL_SETUP.md: Render bloquea los puertos SMTP salientes en el plan Free).
@Component
@Profile("local")
class SmtpMailPort(
    private val mailSender: JavaMailSender
) : MailPort {

    override fun send(from: String, to: String, subject: String, body: String) {
        val mensaje = SimpleMailMessage().apply {
            this.from = from
            setTo(to)
            this.subject = subject
            text = body
        }
        mailSender.send(mensaje)
    }
}

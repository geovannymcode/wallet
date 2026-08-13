package com.geovannycode.wallet.notification.messaging

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class DeadLetterListener(
    private val mailSender: JavaMailSender,
    @Value("\${wallet.mail.from}") private val mailFrom: String,
    @Value("\${wallet.mail.ops-to}") private val opsTo: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["wallet.movements.DLT"], groupId = "dlt-demo")
    fun onDead(record: ConsumerRecord<String, String>) {
        log.warn("💀 Mensaje muerto en el DLT: key={} value={}", record.key(), record.value())

        // Notificación visual para la demo: el correo aparece en Mailpit (http://localhost:8025).
        val mensaje = SimpleMailMessage().apply {
            from = mailFrom
            setTo(opsTo)
            subject = "⚠️ Mensaje enviado a la DLQ: wallet.movements.DLT"
            text = "Un evento no pudo procesarse tras los reintentos y fue apartado a la DLQ.\n\n" +
                "key: ${record.key()}\n" +
                "value: ${record.value()}\n" +
                "topic origen: wallet.movements\n" +
                "partition: ${record.partition()}, offset: ${record.offset()}"
        }
        // El evento muerto ya quedó registrado en el log de arriba; que el envío
        // de correo falle (por ejemplo, un 403 de Resend) no debe tumbar el consumo.
        runCatching { mailSender.send(mensaje) }
            .onFailure { log.error("No se pudo notificar la DLQ por correo", it) }
    }
}
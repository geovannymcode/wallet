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
    @Value("\${wallet.mail.from}") private val mailFrom: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["wallet.movements.DLT"], groupId = "dlt-demo")
    fun onDead(record: ConsumerRecord<String, String>) {
        log.warn("💀 Mensaje muerto en el DLT: key={} value={}", record.key(), record.value())

        // Notificación visual para la demo: el correo aparece en Mailpit (http://localhost:8025).
        val mensaje = SimpleMailMessage().apply {
            from = mailFrom
            setTo("ops@baqjug.com")
            subject = "⚠️ Mensaje enviado a la DLQ: wallet.movements.DLT"
            text = "Un evento no pudo procesarse tras los reintentos y fue apartado a la DLQ.\n\n" +
                "key: ${record.key()}\n" +
                "value: ${record.value()}\n" +
                "topic origen: wallet.movements\n" +
                "partition: ${record.partition()}, offset: ${record.offset()}"
        }
        mailSender.send(mensaje)
    }
}
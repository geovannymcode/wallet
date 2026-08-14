package com.geovannycode.wallet.notification.messaging

import com.geovannycode.wallet.notification.domain.MailPort
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class DeadLetterListener(
    private val mailPort: MailPort,
    @Value("\${wallet.mail.from}") private val mailFrom: String,
    @Value("\${wallet.mail.ops-to}") private val opsTo: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // DeadLetterPublishingRecoverer, sin resolver custom, usa por defecto el
    // sufijo "-dlt" (cr.topic() + "-dlt"), no ".DLT".
    @KafkaListener(topics = ["wallet.movements-dlt"], groupId = "dlt-demo")
    fun onDead(record: ConsumerRecord<String, String>) {
        log.warn("💀 Mensaje muerto en el DLT: key={} value={}", record.key(), record.value())

        // Notificación visual para la demo: en local el correo aparece en Mailpit
        // (http://localhost:8025); en Render sale por la API HTTP de Resend.
        // El evento muerto ya quedó registrado en el log de arriba; que el envío
        // de correo falle no debe tumbar el consumo.
        runCatching {
            mailPort.send(
                from = mailFrom,
                to = opsTo,
                subject = "⚠️ Mensaje enviado a la DLQ: wallet.movements-dlt",
                body = "Un evento no pudo procesarse tras los reintentos y fue apartado a la DLQ.\n\n" +
                    "key: ${record.key()}\n" +
                    "value: ${record.value()}\n" +
                    "topic origen: wallet.movements\n" +
                    "partition: ${record.partition()}, offset: ${record.offset()}"
            )
        }.onFailure { log.error("No se pudo notificar la DLQ por correo", it) }
    }
}
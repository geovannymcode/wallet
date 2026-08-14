package com.geovannycode.wallet.notification.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.Duration

// Producción (Render): Render bloquea el tráfico saliente por los puertos SMTP
// (25, 465, 587) en el plan Free, así que en vez de SMTP usamos la API HTTP de
// Resend (https://resend.com/docs/api-reference/emails/send-email) sobre 443.
@Component
@Profile("!local")
class ResendMailPort(
    @Value("\${wallet.mail.resend-api-key}") private val apiKey: String
) : MailPort {

    private val client = RestClient.builder()
        .baseUrl("https://api.resend.com")
        .requestFactory(
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(Duration.ofSeconds(5))
                setReadTimeout(Duration.ofSeconds(5))
            }
        )
        .build()

    override fun send(from: String, to: String, subject: String, body: String) {
        client.post()
            .uri("/emails")
            .header("Authorization", "Bearer $apiKey")
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("from" to from, "to" to listOf(to), "subject" to subject, "text" to body))
            .retrieve()
            .toBodilessEntity()
    }
}

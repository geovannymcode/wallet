package com.geovannycode.wallet.notification.domain

import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity

@Component
class PushClient(private val webClient: WebClient) {
    suspend fun enviar(evento: MovimientoRegistrado) {
        webClient.post()
            .uri("/push")
            .bodyValue(evento)
            .retrieve()
            .awaitBodilessEntity()   // suspende aquí, sin bloquear el hilo
    }
}

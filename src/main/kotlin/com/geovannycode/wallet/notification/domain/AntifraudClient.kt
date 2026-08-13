package com.geovannycode.wallet.notification.domain

import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity

@Component
class AntifraudClient(private val webClient: WebClient) {
    suspend fun revisar(evento: MovimientoRegistrado) {
        webClient.post()
            .uri("/antifraud")
            .bodyValue(evento)
            .retrieve()
            .awaitBodilessEntity()   // suspende aquí, sin bloquear el hilo
    }
}

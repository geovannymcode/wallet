package com.geovannycode.wallet.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    // Apunta a los endpoints mock de /mock-external (ver MockExternalController),
    // que simulan correo, push y antifraude para la Fase 9 (coroutines) del taller.
    @Bean
    fun webClient(
        @Value("\${wallet.notification.external-base-url:http://localhost:8080/mock-external}") baseUrl: String
    ): WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build()
}

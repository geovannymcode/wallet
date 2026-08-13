package com.geovannycode.wallet.notification.mock

import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Simula 3 servicios externos (correo, push, antifraude) para la Fase 9 del taller
 * (coroutines). Cada endpoint duerme ~200 ms para representar la latencia de red de
 * un servicio real; así el ejercicio de "3 llamadas en paralelo" es reproducible
 * 100% en local, sin depender de infraestructura externa.
 *
 * SOLO PARA LA DEMO: no reemplaza el envío real de correo (EmailNotifier + Mailpit).
 */
@RestController
@RequestMapping("/mock-external")
class MockExternalController {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/emails")
    fun emails(@RequestBody evento: MovimientoRegistrado): ResponseEntity<Void> {
        simularLatencia("email", evento)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/push")
    fun push(@RequestBody evento: MovimientoRegistrado): ResponseEntity<Void> {
        simularLatencia("push", evento)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/antifraud")
    fun antifraud(@RequestBody evento: MovimientoRegistrado): ResponseEntity<Void> {
        simularLatencia("antifraude", evento)
        return ResponseEntity.accepted().build()
    }

    private fun simularLatencia(servicio: String, evento: MovimientoRegistrado) {
        log.info("🐢 [mock-external] {} procesando evento {} (~200ms)...", servicio, evento.eventId)
        Thread.sleep(200)
        log.info("✅ [mock-external] {} terminó evento {}", servicio, evento.eventId)
    }
}

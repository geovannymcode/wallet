package com.geovannycode.wallet.transfer.web

import com.geovannycode.wallet.transfer.domain.TransferRequest
import com.geovannycode.wallet.transfer.domain.TransferService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/transfers")
class TransferController(
    private val transferService: TransferService
) {

    @PostMapping
    fun transfer(@Valid @RequestBody request: TransferRequest): ResponseEntity<Map<String, String>> {
        transferService.transfer(request)
        return ResponseEntity.ok(mapOf("status" to "COMPLETED"))
    }
}
package com.geovannycode.wallet.transfer.internal

import com.geovannycode.wallet.account.api.MoveResult
import com.geovannycode.wallet.transfer.api.TransferRequest
import com.geovannycode.wallet.transfer.api.TransferService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
    fun transfer(@Valid @RequestBody request: TransferRequest): ResponseEntity<Any> =
        when (val result = transferService.transfer(request)) {
            is MoveResult.Success ->
                ResponseEntity.ok(mapOf("status" to "COMPLETED"))
            is MoveResult.InsufficientFunds ->
                ResponseEntity.unprocessableEntity().body(mapOf("error" to "Saldo insuficiente"))
            is MoveResult.AccountNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("error" to "No existe la cuenta ${result.id}"))
        }
}
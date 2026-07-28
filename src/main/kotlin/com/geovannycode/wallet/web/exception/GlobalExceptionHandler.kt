package com.geovannycode.wallet.web.exception

import com.geovannycode.wallet.account.domain.AccountNotFoundException
import com.geovannycode.wallet.account.domain.InsufficientFundsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException::class)
    fun handleNotFound(ex: AccountNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to (ex.message ?: "Cuenta no encontrada")))

    @ExceptionHandler(InsufficientFundsException::class)
    fun handleInsufficientFunds(ex: InsufficientFundsException): ResponseEntity<Map<String, String>> =
        ResponseEntity.unprocessableEntity()
            .body(mapOf("error" to (ex.message ?: "Saldo insuficiente")))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest()
            .body(mapOf("error" to (ex.message ?: "Petición inválida")))
}
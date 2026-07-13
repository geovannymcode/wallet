package com.geovannycode.wallet.account.internal

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AccountExceptionHandler {

    @ExceptionHandler(AccountNotFoundException::class)
    fun handleNotFound(ex: AccountNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to (ex.message ?: "Cuenta no encontrada")))
}
package com.geovannycode.wallet.transfer.domain

import com.geovannycode.wallet.account.domain.AccountNotFoundException
import com.geovannycode.wallet.account.domain.AccountService
import com.geovannycode.wallet.account.domain.InsufficientFundsException
import com.geovannycode.wallet.movement.domain.MoveResult
import com.geovannycode.wallet.movement.domain.MovementService
import org.springframework.stereotype.Service

@Service
class TransferService(
    private val accountService: AccountService,
    private val movementService: MovementService
) {

    fun transfer(request: TransferRequest) {
        require(request.fromId != request.toId) {
            "No puedes transferir a la misma cuenta"
        }
        // Movemos la plata y miramos cómo salió.
        when (val result = accountService.moveMoney(request.fromId, request.toId, request.amount)) {
            is MoveResult.Success ->
                // Llamada directa y síncrona. Anota esto: acá está la costura.
                movementService.record(request.fromId, request.toId, request.amount)
            is MoveResult.InsufficientFunds -> throw InsufficientFundsException()
            is MoveResult.AccountNotFound -> throw AccountNotFoundException(result.id)
        }
    }
}
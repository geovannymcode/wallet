package com.geovannycode.wallet.transfer.internal

import com.geovannycode.wallet.account.api.AccountService
import com.geovannycode.wallet.account.api.MoveResult
import com.geovannycode.wallet.movement.api.MovementService
import com.geovannycode.wallet.transfer.api.TransferRequest
import com.geovannycode.wallet.transfer.api.TransferService
import org.springframework.stereotype.Service

@Service
class DefaultTransferService(
    private val accountService: AccountService,
    private val movementService: MovementService
) : TransferService {

    override fun transfer(request: TransferRequest): MoveResult {
        require(request.fromId != request.toId) {
            "No puedes transferir a la misma cuenta"
        }
        val result = accountService.moveMoney(request.fromId, request.toId, request.amount)

        if (result is MoveResult.Success) {
            // Llamada directa y síncrona. Anota esto: acá está la costura.
            movementService.record(request.fromId, request.toId, request.amount)
        }
        return result
    }
}
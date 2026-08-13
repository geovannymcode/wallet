package com.geovannycode.wallet.transfer.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.geovannycode.wallet.account.domain.AccountNotFoundException
import com.geovannycode.wallet.account.domain.AccountService
import com.geovannycode.wallet.account.domain.InsufficientFundsException
import com.geovannycode.wallet.movement.domain.MoveResult
import com.geovannycode.wallet.movement.domain.MovimientoRegistrado
import com.geovannycode.wallet.transfer.messaging.OutboxEvent
import com.geovannycode.wallet.transfer.messaging.OutboxRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TransferService(
    private val accountService: AccountService,
    private val outbox: OutboxRepository,
    private val mapper: ObjectMapper
) {

    @Transactional
    fun transfer(request: TransferRequest) {
        require(request.fromId != request.toId) {
            "No puedes transferir a la misma cuenta"
        }
        // Movemos la plata y miramos cómo salió.
        when (val result = accountService.moveMoney(request.fromId, request.toId, request.amount)) {
            is MoveResult.Success -> {
                val evento = MovimientoRegistrado(fromId = request.fromId, toId = request.toId, amount = request.amount)
                outbox.save(
                    OutboxEvent(
                        topic = "wallet.movements",
                        msgKey = evento.fromId.toString(),
                        payload = mapper.writeValueAsString(evento)
                    )
                )
            }

            is MoveResult.InsufficientFunds -> throw InsufficientFundsException()
            is MoveResult.AccountNotFound -> throw AccountNotFoundException(result.id)
        }
    }
}
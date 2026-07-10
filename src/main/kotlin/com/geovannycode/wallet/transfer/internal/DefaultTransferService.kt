package com.geovannycode.wallet.transfer.internal

import com.geovannycode.wallet.account.api.AccountService
import com.geovannycode.wallet.account.api.MoveResult
import com.geovannycode.wallet.transfer.api.TransferRequest
import com.geovannycode.wallet.transfer.api.TransferService
import org.springframework.stereotype.Service

@Service
class DefaultTransferService(
    private val accountService: AccountService
) : TransferService {

    override fun transfer(request: TransferRequest): MoveResult {
        require(request.fromId != request.toId) {
            "No puedes transferir a la misma cuenta"
        }
        return accountService.moveMoney(request.fromId, request.toId, request.amount)
    }
}
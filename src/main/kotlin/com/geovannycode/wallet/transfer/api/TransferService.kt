package com.geovannycode.wallet.transfer.api

import com.geovannycode.wallet.account.api.MoveResult

interface TransferService {
    fun transfer(request: TransferRequest): MoveResult
}
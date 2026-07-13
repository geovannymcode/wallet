package com.geovannycode.wallet.account.api

import java.math.BigDecimal
import java.util.UUID

interface AccountService {
    fun getById(id: UUID): AccountResponse
    fun moveMoney(fromId: UUID, toId: UUID, amount: BigDecimal): MoveResult
}
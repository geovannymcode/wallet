package com.geovannycode.wallet.account.api

import java.util.UUID

sealed class MoveResult {
    data object Success : MoveResult()
    data object InsufficientFunds : MoveResult()
    data class AccountNotFound(val id: UUID) : MoveResult()
}
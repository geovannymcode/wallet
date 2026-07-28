package com.geovannycode.wallet.movement.domain

import java.util.UUID

sealed class MoveResult {
    data object Success : MoveResult()
    data object InsufficientFunds : MoveResult()
    data class AccountNotFound(val id: UUID) : MoveResult()
}
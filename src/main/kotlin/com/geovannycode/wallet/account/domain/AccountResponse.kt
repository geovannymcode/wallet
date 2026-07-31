package com.geovannycode.wallet.account.domain

import java.math.BigDecimal
import java.util.UUID

data class AccountResponse(
    val id: UUID,
    val owner: String,
    val email: String,
    val balance: BigDecimal
)

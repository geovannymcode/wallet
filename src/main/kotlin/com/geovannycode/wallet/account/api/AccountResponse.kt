package com.geovannycode.wallet.account.api

import java.math.BigDecimal
import java.util.UUID

data class AccountResponse(
    val id: UUID,
    val owner: String,
    val balance: BigDecimal
)

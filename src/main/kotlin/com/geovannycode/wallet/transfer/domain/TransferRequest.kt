package com.geovannycode.wallet.transfer.domain

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.UUID

data class TransferRequest(
    @field:NotNull
    val fromId: UUID,

    @field:NotNull
    val toId: UUID,

    @field:NotNull
    @field:DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    val amount: BigDecimal
)
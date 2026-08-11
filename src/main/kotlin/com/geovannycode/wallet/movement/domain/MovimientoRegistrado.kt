package com.geovannycode.wallet.movement.domain

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class MovimientoRegistrado(
    val fromId: UUID,
    val toId: UUID,
    val amount: BigDecimal,
    val occurredAt: Instant = Instant.now()
)
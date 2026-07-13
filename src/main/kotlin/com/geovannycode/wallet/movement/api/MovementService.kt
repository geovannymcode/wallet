package com.geovannycode.wallet.movement.api

import java.math.BigDecimal
import java.util.UUID

interface MovementService {
    fun record(fromId: UUID, toId: UUID, amount: BigDecimal)
}
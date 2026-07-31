package com.geovannycode.wallet.movement.domain

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional
class MovementService(
    private val repository: MovementRepository
) {

    fun record(fromId: UUID, toId: UUID, amount: BigDecimal) {
        repository.save(MovementEntity(fromId = fromId, toId = toId, amount = amount))
    }
}
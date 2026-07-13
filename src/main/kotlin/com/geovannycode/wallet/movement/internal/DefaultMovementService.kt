package com.geovannycode.wallet.movement.internal

import com.geovannycode.wallet.movement.api.MovementService

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional
class DefaultMovementService(
    private val repository: MovementRepository
) : MovementService {

    override fun record(fromId: UUID, toId: UUID, amount: BigDecimal) {
        repository.save(Movement(fromId = fromId, toId = toId, amount = amount))
    }
}
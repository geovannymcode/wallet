package com.geovannycode.wallet.transfer.messaging

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OutboxRepository : JpaRepository<OutboxEvent, UUID> {
    fun findBySentAtIsNullOrderByCreatedAt(): List<OutboxEvent>
}
package com.geovannycode.wallet.movement.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "movements")
class Movement(
    @Column(nullable = false)
    val fromId: UUID,

    @Column(nullable = false)
    val toId: UUID,

    @Column(nullable = false, precision = 19, scale = 2)
    val amount: BigDecimal,

    @Column(nullable = false)
    val occurredAt: Instant = Instant.now(),

    @Id
    val id: UUID = UUID.randomUUID()
)

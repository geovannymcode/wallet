package com.geovannycode.wallet.account.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "accounts")
class Account(
    @Column(nullable = false)
    val owner: String,

    @Column(nullable = false, precision = 19, scale = 2)
    var balance: BigDecimal = BigDecimal.ZERO,

    @Id
    val id: UUID = UUID.randomUUID()
)
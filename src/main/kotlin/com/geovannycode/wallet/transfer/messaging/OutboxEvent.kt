package com.geovannycode.wallet.transfer.messaging

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "outbox")
class OutboxEvent(
    @Column(nullable = false)
    val topic: String,

    @Column(name = "msg_key", nullable = false)
    val msgKey: String,

    @Column(nullable = false)
    val payload: String,

    @Column(name = "sent_at")
    var sentAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Id
    val id: UUID = UUID.randomUUID()
)
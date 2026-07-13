package com.geovannycode.wallet

import com.geovannycode.wallet.account.api.AccountResponse
import com.geovannycode.wallet.account.api.AccountService
import com.geovannycode.wallet.account.api.MoveResult
import com.geovannycode.wallet.movement.api.MovementService
import com.geovannycode.wallet.transfer.api.TransferRequest
import com.geovannycode.wallet.transfer.internal.DefaultTransferService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID


class DefaultTransferServiceTest {
    // Doble de prueba: siempre dice que el movimiento salió bien.
    private val accountOk = object : AccountService {
        override fun getById(id: UUID) = AccountResponse(id, "test", BigDecimal.TEN)
        override fun moveMoney(fromId: UUID, toId: UUID, amount: BigDecimal) = MoveResult.Success
    }

    // Doble de prueba: cuenta cuántas veces le pidieron registrar.
    private class RecordingMovements : MovementService {
        var calls = 0
        override fun record(fromId: UUID, toId: UUID, amount: BigDecimal) { calls++ }
    }

    @Test
    fun `registra el movimiento cuando la transferencia se completa`() {
        val movements = RecordingMovements()
        val service = DefaultTransferService(accountOk, movements)

        val result = service.transfer(
            TransferRequest(UUID.randomUUID(), UUID.randomUUID(), BigDecimal("5.00"))
        )

        assertTrue(result is MoveResult.Success)
        assertEquals(1, movements.calls)
    }
}
package com.geovannycode.wallet.transfer.domain

import com.geovannycode.wallet.account.domain.AccountService
import com.geovannycode.wallet.account.domain.InsufficientFundsException
import com.geovannycode.wallet.movement.domain.MoveResult
import com.geovannycode.wallet.movement.domain.MovementService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class TransferServiceTest {

    private val accountService = mockk<AccountService>()
    private val movementService = mockk<MovementService>(relaxed = true)
    private val transferService = TransferService(accountService, movementService)

    @Test
    fun `registra el movimiento cuando la transferencia se completa`() {
        every { accountService.moveMoney(any(), any(), any()) } returns MoveResult.Success

        transferService.transfer(
            TransferRequest(UUID.randomUUID(), UUID.randomUUID(), BigDecimal("5.00"))
        )

        verify(exactly = 1) { movementService.record(any(), any(), any()) }
    }

    @Test
    fun `lanza excepcion y no registra cuando no hay saldo`() {
        every { accountService.moveMoney(any(), any(), any()) } returns MoveResult.InsufficientFunds

        assertThrows(InsufficientFundsException::class.java) {
            transferService.transfer(
                TransferRequest(UUID.randomUUID(), UUID.randomUUID(), BigDecimal("5.00"))
            )
        }

        verify(exactly = 0) { movementService.record(any(), any(), any()) }
    }
}
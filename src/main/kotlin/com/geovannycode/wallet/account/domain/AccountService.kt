package com.geovannycode.wallet.account.domain

import com.geovannycode.wallet.movement.domain.MoveResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional
class AccountService(
    private val repository: AccountRepository
) {

    fun getById(id: UUID): AccountResponse {
        val account = repository.findById(id)
            .orElseThrow { AccountNotFoundException(id) }
        return AccountMapper.toResponse(account)
    }

    fun moveMoney(fromId: UUID, toId: UUID, amount: BigDecimal): MoveResult {
        val from = repository.findById(fromId).orElse(null)
            ?: return MoveResult.AccountNotFound(fromId)
        val to = repository.findById(toId).orElse(null)
            ?: return MoveResult.AccountNotFound(toId)

        if (from.balance < amount) {
            return MoveResult.InsufficientFunds
        }

        from.balance = from.balance.subtract(amount)
        to.balance = to.balance.add(amount)
        repository.save(from)
        repository.save(to)

        return MoveResult.Success
    }

}
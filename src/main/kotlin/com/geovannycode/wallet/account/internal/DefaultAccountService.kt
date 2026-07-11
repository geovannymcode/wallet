package com.geovannycode.wallet.account.internal

import com.geovannycode.wallet.account.api.AccountResponse
import com.geovannycode.wallet.account.api.AccountService
import com.geovannycode.wallet.account.api.MoveResult
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional(readOnly = true)
class DefaultAccountService(
    private val repository: AccountRepository
) : AccountService {

    override fun getById(id: UUID): AccountResponse {
        val account = repository.findById(id)
            .orElseThrow { AccountNotFoundException(id) }
        return AccountResponse(account.id, account.owner, account.balance)
    }

    @Transactional
    override fun moveMoney(fromId: UUID, toId: UUID, amount: BigDecimal): MoveResult {
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

class AccountNotFoundException(id: UUID) :
    RuntimeException("No existe la cuenta $id")
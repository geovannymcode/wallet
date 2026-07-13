package com.geovannycode.wallet.account.internal

import com.geovannycode.wallet.account.api.AccountResponse
import com.geovannycode.wallet.account.api.AccountService
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
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
}

class AccountNotFoundException(id: UUID) :
    RuntimeException("No existe la cuenta $id")
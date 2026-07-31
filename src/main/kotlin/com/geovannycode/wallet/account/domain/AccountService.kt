package com.geovannycode.wallet.account.domain

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class AccountService(
    private val repository: AccountRepository
) {

    fun getById(id: UUID): AccountResponse {
        val account = repository.findById(id)
            .orElseThrow { AccountNotFoundException(id) }
        return AccountMapper.toResponse(account)
    }
}
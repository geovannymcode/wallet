package com.geovannycode.wallet.account.api

import java.util.UUID

interface AccountService {
    fun getById(id: UUID): AccountResponse
}
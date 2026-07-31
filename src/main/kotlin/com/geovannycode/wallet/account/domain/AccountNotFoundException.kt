package com.geovannycode.wallet.account.domain

import java.util.UUID

class AccountNotFoundException(id: UUID) :
    RuntimeException("No existe la cuenta $id")
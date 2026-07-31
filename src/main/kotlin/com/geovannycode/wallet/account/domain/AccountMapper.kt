package com.geovannycode.wallet.account.domain

object AccountMapper {
    fun toResponse(entity: AccountEntity) = AccountResponse(
        id = entity.id,
        owner = entity.owner,
        email = entity.email,
        balance = entity.balance
    )
}
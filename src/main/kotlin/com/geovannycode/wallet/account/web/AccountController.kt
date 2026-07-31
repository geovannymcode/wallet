package com.geovannycode.wallet.account.web

import com.geovannycode.wallet.account.domain.AccountResponse
import com.geovannycode.wallet.account.domain.AccountService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val accountService: AccountService
) {

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): AccountResponse =
        accountService.getById(id)
}
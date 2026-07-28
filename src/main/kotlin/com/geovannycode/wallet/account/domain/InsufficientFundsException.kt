package com.geovannycode.wallet.account.domain

class InsufficientFundsException :
    RuntimeException("Saldo insuficiente para la transferencia")
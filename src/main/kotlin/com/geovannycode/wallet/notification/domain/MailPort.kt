package com.geovannycode.wallet.notification.domain

interface MailPort {
    fun send(from: String, to: String, subject: String, body: String)
}

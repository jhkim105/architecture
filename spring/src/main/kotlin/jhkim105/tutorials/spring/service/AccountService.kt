package jhkim105.tutorials.spring.service

import jhkim105.tutorials.spring.model.Account
import java.math.BigDecimal

interface AccountService {
    fun get(id: String): Account
    fun create(initialBalance: BigDecimal): Account
    fun deposit(accountId: String, amount: BigDecimal): Account
    fun withdraw(accountId: String, amount: BigDecimal): Account
    fun transfer(fromAccountId: String, toAccountId: String, amount: BigDecimal): Account
}
package jhkim105.tutorials.onion.application

import jhkim105.tutorials.onion.domain.model.Account
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
interface AccountService {
    fun get(id: String): Account
    fun create(initialBalance: BigDecimal): Account
    fun deposit(accountId: String, amount: BigDecimal): Account
    fun withdraw(accountId: String, amount: BigDecimal): Account
    fun transfer(fromAccountId: String, toAccountId: String, amount: BigDecimal): Account


}
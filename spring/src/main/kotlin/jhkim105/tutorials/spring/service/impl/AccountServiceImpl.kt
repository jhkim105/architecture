package jhkim105.tutorials.spring.service.impl

import jhkim105.tutorials.spring.model.Account
import jhkim105.tutorials.spring.repository.AccountRepository
import jhkim105.tutorials.spring.service.AccountService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*


@Service
class AccountServiceImpl(
    private val accountRepository: AccountRepository
) : AccountService {
    override fun get(accountId: String): Account {
        return accountRepository.findByIdOrNull(accountId) ?: throw IllegalArgumentException("data not found")
    }

    override fun create(initialBalance: BigDecimal): Account {
        val accountId = UUID.randomUUID().toString()
        val newAccount = Account(accountId, initialBalance)
        return accountRepository.save(newAccount)
    }

    override fun deposit(accountId: String, amount: BigDecimal): Account {
        val account = get(accountId)
        account.deposit(amount)
        accountRepository.save(account)
        return get(accountId)
    }

    override fun withdraw(accountId: String, amount: BigDecimal): Account {
        val account = get(accountId)
        account.withdraw(amount)
        accountRepository.save(account)
        return get(accountId)
    }

    override fun transfer(fromAccountId: String, toAccountId: String, amount: BigDecimal): Account {
        val fromAccount = get(fromAccountId)
        val toAccount = get(toAccountId)
        fromAccount.transfer(toAccount, amount)
        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)
        return get(fromAccountId)
    }

}
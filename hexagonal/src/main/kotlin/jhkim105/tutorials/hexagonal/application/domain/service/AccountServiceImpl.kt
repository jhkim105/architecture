package jhkim105.tutorials.hexagonal.application.domain.service

import jhkim105.tutorials.hexagonal.adapter.out.persistence.AccountJpaEntity
import jhkim105.tutorials.hexagonal.application.domain.model.Account
import jhkim105.tutorials.hexagonal.application.port.`in`.AccountService
import jhkim105.tutorials.hexagonal.application.port.out.AccountRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import javax.security.auth.login.AccountNotFoundException


@Service
class AccountServiceImpl(
    private val accountRepository: AccountRepository
) : AccountService {
    override fun get(accountId: String): Account {
        return accountRepository.findById(accountId) ?: throw IllegalArgumentException("Account of $accountId not found")
    }

    override fun create(initialBalance: BigDecimal): Account {
        val accountId = UUID.randomUUID().toString()
        val newAccount = Account(accountId, initialBalance)
        return accountRepository.save(newAccount)
    }

    override fun deposit(accountId: String, amount: BigDecimal): Account {
        val account = get(accountId)
        account.deposit(amount)
        return accountRepository.save(account)
    }

    override fun withdraw(accountId: String, amount: BigDecimal): Account {
        val account = get(accountId)
        account.withdraw(amount)
        return accountRepository.save(account)
    }

    override fun transfer(fromAccountId: String, toAccountId: String, amount: BigDecimal): Account {
        val fromAccount = get(fromAccountId)
        val toAccount = get(toAccountId)
        fromAccount.transfer(toAccount, amount)
        accountRepository.save(fromAccount)
        return accountRepository.save(toAccount)
    }

}
package jhkim105.tutorials.application.domain.service

import jhkim105.tutorials.application.domain.model.Account
import jhkim105.tutorials.application.port.`in`.AccountService
import jhkim105.tutorials.application.port.out.AccountRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal


@Service
class AccountServiceImpl(
    private val accountRepository: AccountRepository
) : AccountService {
    override fun get(accountId: String): Account {
        return accountRepository.findById(accountId)
    }

    override fun deposit(accountId: String, amount: BigDecimal): Account {
        val account = accountRepository.findById(accountId)
        account.deposit(amount)
        return accountRepository.save(account)
    }

    override fun withdraw(accountId: String, amount: BigDecimal): Account {
        val account = accountRepository.findById(accountId)
        account.withdraw(amount)
        return accountRepository.save(account)
    }

    override fun transfer(fromAccountId: String, toAccountId: String, amount: BigDecimal): Account {
        val fromAccount = accountRepository.findById(fromAccountId)
        val toAccount = accountRepository.findById(toAccountId)
        fromAccount.transfer(toAccount, amount)
        accountRepository.save(fromAccount)
        return accountRepository.save(toAccount)
    }

}
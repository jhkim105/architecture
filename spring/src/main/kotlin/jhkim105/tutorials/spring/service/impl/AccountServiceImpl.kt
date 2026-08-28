package jhkim105.tutorials.spring.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.tutorials.spring.model.Account
import jhkim105.tutorials.spring.repository.AccountRepository
import jhkim105.tutorials.spring.service.AccountService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * Service Implementation:
 * Repository에 직접 의존하며, 트랜잭션(@Transactional) 경계 내에서 엔티티의 비즈니스 메서드를 호출합니다.
 */
@Service
@Transactional
class AccountServiceImpl(
    private val accountRepository: AccountRepository
) : AccountService {

    @Transactional(readOnly = true)
    override fun get(id: String): Account {
        return accountRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Account of id $id not found")
    }

    override fun create(initialBalance: BigDecimal): Account {
        val accountId = UUID.randomUUID().toString()
        val newAccount = Account(accountId, initialBalance)
        log.info { "Creating account: $accountId with initial balance: $initialBalance" }
        return accountRepository.save(newAccount)
    }

    override fun deposit(accountId: String, amount: BigDecimal): Account {
        val account = get(accountId)
        account.deposit(amount)
        log.info { "Deposited $amount to account $accountId" }
        return accountRepository.save(account)
    }

    override fun withdraw(accountId: String, amount: BigDecimal): Account {
        val account = get(accountId)
        account.withdraw(amount)
        log.info { "Withdrew $amount from account $accountId" }
        return accountRepository.save(account)
    }

    override fun transfer(fromAccountId: String, toAccountId: String, amount: BigDecimal): Account {
        val fromAccount = get(fromAccountId)
        val toAccount = get(toAccountId)

        fromAccount.transfer(toAccount, amount)

        accountRepository.save(fromAccount)
        val savedToAccount = accountRepository.save(toAccount)

        log.info { "Transferred $amount from $fromAccountId to $toAccountId" }
        return savedToAccount
    }
}

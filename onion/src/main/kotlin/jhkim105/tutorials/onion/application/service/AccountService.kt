package jhkim105.tutorials.onion.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.tutorials.onion.application.dto.*
import jhkim105.tutorials.onion.domain.model.Account
import jhkim105.tutorials.onion.domain.repository.AccountRepository
import jhkim105.tutorials.onion.domain.service.AccountTransferDomainService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * 3. Application Services Layer:
 * 유스케이스 흐름을 관장하고 트랜잭션 경계를 제어하며, 도메인 레포지토리와 도메인 서비스를 조율(Orchestration)합니다.
 */
@Service
@Transactional
class AccountService(
    private val accountRepository: AccountRepository,
    private val accountTransferDomainService: AccountTransferDomainService
) {

    @Transactional(readOnly = true)
    fun get(query: GetAccountQuery): Account {
        return accountRepository.findById(query.accountId)
            ?: throw NoSuchElementException("Account of id ${query.accountId} not found")
    }

    fun create(command: CreateAccountCommand): Account {
        val accountId = UUID.randomUUID().toString()
        val newAccount = Account(id = accountId, balance = command.initialBalance)
        log.info { "Creating account: $accountId with initial balance: ${command.initialBalance}" }
        return accountRepository.save(newAccount)
    }

    fun deposit(command: DepositCommand): Account {
        val account = get(GetAccountQuery(command.accountId))
        account.deposit(command.amount)
        log.info { "Deposited ${command.amount} to account ${command.accountId}" }
        return accountRepository.save(account)
    }

    fun withdraw(command: WithdrawCommand): Account {
        val account = get(GetAccountQuery(command.accountId))
        account.withdraw(command.amount)
        log.info { "Withdrew ${command.amount} from account ${command.accountId}" }
        return accountRepository.save(account)
    }

    fun transfer(command: TransferCommand): Account {
        val fromAccount = get(GetAccountQuery(command.fromAccountId))
        val toAccount = get(GetAccountQuery(command.toAccountId))

        // 도메인 서비스에 비즈니스 정책 위임
        accountTransferDomainService.transfer(fromAccount, toAccount, command.amount)

        accountRepository.save(fromAccount)
        val savedToAccount = accountRepository.save(toAccount)

        log.info { "Transferred ${command.amount} from ${command.fromAccountId} to ${command.toAccountId}" }
        return savedToAccount
    }
}

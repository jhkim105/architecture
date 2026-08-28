package jhkim105.tutorials.hexagonal.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.tutorials.hexagonal.application.domain.model.Account
import jhkim105.tutorials.hexagonal.application.domain.service.AccountTransferDomainService
import jhkim105.tutorials.hexagonal.application.port.`in`.*
import jhkim105.tutorials.hexagonal.application.port.out.LoadAccountPort
import jhkim105.tutorials.hexagonal.application.port.out.SaveAccountPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * 애플리케이션 서비스: 유스케이스 흐름 제어, 트랜잭션 관리, 영속성 포트 호출 및 도메인 서비스를 조율(Orchestration)합니다.
 */
@Service
@Transactional
class AccountService(
    private val loadAccountPort: LoadAccountPort,
    private val saveAccountPort: SaveAccountPort,
    private val accountTransferDomainService: AccountTransferDomainService
) : CreateAccountUseCase, DepositUseCase, WithdrawUseCase, TransferUseCase, GetAccountUseCase {

    @Transactional(readOnly = true)
    override fun get(query: GetAccountQuery): Account {
        return loadAccountPort.loadAccount(query.accountId)
            ?: throw NoSuchElementException("Account of id ${query.accountId} not found")
    }

    override fun create(command: CreateAccountCommand): Account {
        val accountId = UUID.randomUUID().toString()
        val newAccount = Account(id = accountId, balance = command.initialBalance)
        log.info { "Creating new account: $accountId with balance: ${command.initialBalance}" }
        return saveAccountPort.saveAccount(newAccount)
    }

    override fun deposit(command: DepositCommand): Account {
        val account = get(GetAccountQuery(command.accountId))
        account.deposit(command.amount)
        log.info { "Deposited ${command.amount} to account ${command.accountId}" }
        return saveAccountPort.saveAccount(account)
    }

    override fun withdraw(command: WithdrawCommand): Account {
        val account = get(GetAccountQuery(command.accountId))
        account.withdraw(command.amount)
        log.info { "Withdrew ${command.amount} from account ${command.accountId}" }
        return saveAccountPort.saveAccount(account)
    }

    override fun transfer(command: TransferCommand): Account {
        val fromAccount = get(GetAccountQuery(command.fromAccountId))
        val toAccount = get(GetAccountQuery(command.toAccountId))

        // 도메인 서비스를 통한 비즈니스 규칙 검증 및 상태 변경
        accountTransferDomainService.transfer(fromAccount, toAccount, command.amount)

        // 변경된 상태 영속화
        saveAccountPort.saveAccount(fromAccount)
        val savedToAccount = saveAccountPort.saveAccount(toAccount)

        log.info { "Transferred ${command.amount} from ${command.fromAccountId} to ${command.toAccountId}" }
        return savedToAccount
    }
}

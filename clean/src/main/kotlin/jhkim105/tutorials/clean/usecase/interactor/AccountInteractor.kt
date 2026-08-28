package jhkim105.tutorials.clean.usecase.interactor

import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.tutorials.clean.domain.entity.Account
import jhkim105.tutorials.clean.domain.service.AccountTransferDomainService
import jhkim105.tutorials.clean.usecase.boundary.`in`.*
import jhkim105.tutorials.clean.usecase.gateway.LoadAccountGateway
import jhkim105.tutorials.clean.usecase.gateway.SaveAccountGateway
import jhkim105.tutorials.clean.usecase.model.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * Application Business Rules (Use Case Interactor):
 * 비즈니스 유스케이스 흐름을 관장하고 트랜잭션 경계를 제어하며, 게이트웨이 및 도메인 서비스를 조율(Orchestration)합니다.
 */
@Service
@Transactional
class AccountInteractor(
    private val loadAccountGateway: LoadAccountGateway,
    private val saveAccountGateway: SaveAccountGateway,
    private val accountTransferDomainService: AccountTransferDomainService
) : CreateAccountInputBoundary, DepositInputBoundary, WithdrawInputBoundary, TransferInputBoundary, GetAccountInputBoundary {

    @Transactional(readOnly = true)
    override fun get(request: GetAccountRequestModel): Account {
        return loadAccountGateway.loadAccount(request.accountId)
            ?: throw NoSuchElementException("Account of id ${request.accountId} not found")
    }

    override fun create(request: CreateAccountRequestModel): Account {
        val accountId = UUID.randomUUID().toString()
        val newAccount = Account(id = accountId, balance = request.initialBalance)
        log.info { "Creating account: $accountId with initial balance: ${request.initialBalance}" }
        return saveAccountGateway.saveAccount(newAccount)
    }

    override fun deposit(request: DepositRequestModel): Account {
        val account = get(GetAccountRequestModel(request.accountId))
        account.deposit(request.amount)
        log.info { "Deposited ${request.amount} to account ${request.accountId}" }
        return saveAccountGateway.saveAccount(account)
    }

    override fun withdraw(request: WithdrawRequestModel): Account {
        val account = get(GetAccountRequestModel(request.accountId))
        account.withdraw(request.amount)
        log.info { "Withdrew ${request.amount} from account ${request.accountId}" }
        return saveAccountGateway.saveAccount(account)
    }

    override fun transfer(request: TransferRequestModel): Account {
        val fromAccount = get(GetAccountRequestModel(request.fromAccountId))
        val toAccount = get(GetAccountRequestModel(request.toAccountId))

        // 도메인 서비스에 비즈니스 규칙 위임
        accountTransferDomainService.transfer(fromAccount, toAccount, request.amount)

        saveAccountGateway.saveAccount(fromAccount)
        val savedToAccount = saveAccountGateway.saveAccount(toAccount)

        log.info { "Transferred ${request.amount} from ${request.fromAccountId} to ${request.toAccountId}" }
        return savedToAccount
    }
}

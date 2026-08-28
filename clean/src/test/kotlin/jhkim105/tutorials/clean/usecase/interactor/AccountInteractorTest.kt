package jhkim105.tutorials.clean.usecase.interactor

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jhkim105.tutorials.clean.domain.entity.Account
import jhkim105.tutorials.clean.domain.service.AccountTransferDomainService
import jhkim105.tutorials.clean.usecase.gateway.LoadAccountGateway
import jhkim105.tutorials.clean.usecase.gateway.SaveAccountGateway
import jhkim105.tutorials.clean.usecase.model.*
import java.math.BigDecimal

class AccountInteractorTest : BehaviorSpec({
    val loadAccountGateway = mockk<LoadAccountGateway>()
    val saveAccountGateway = mockk<SaveAccountGateway>()
    val accountTransferDomainService = AccountTransferDomainService()
    val accountInteractor = AccountInteractor(loadAccountGateway, saveAccountGateway, accountTransferDomainService)

    Given("AccountInteractor 유스케이스가 준비되었을 때") {
        When("신규 계좌 생성을 요청하면") {
            every { saveAccountGateway.saveAccount(any()) } answers { firstArg() }

            val created = accountInteractor.create(CreateAccountRequestModel(BigDecimal("500")))

            Then("초기 잔액을 가진 계좌가 정상 저장된다") {
                created.balance shouldBe BigDecimal("500")
                verify { saveAccountGateway.saveAccount(any()) }
            }
        }

        When("입금을 요청하면") {
            val account = Account("1", BigDecimal("1000"))
            every { loadAccountGateway.loadAccount("1") } returns account
            every { saveAccountGateway.saveAccount(account) } returns account

            val result = accountInteractor.deposit(DepositRequestModel("1", BigDecimal("200")))

            Then("잔액이 입금액만큼 증가하고 저장된다") {
                result.balance shouldBe BigDecimal("1200")
                verify { saveAccountGateway.saveAccount(account) }
            }
        }

        When("출금을 요청하면") {
            val account = Account("1", BigDecimal("1000"))
            every { loadAccountGateway.loadAccount("1") } returns account
            every { saveAccountGateway.saveAccount(account) } returns account

            val result = accountInteractor.withdraw(WithdrawRequestModel("1", BigDecimal("300")))

            Then("잔액이 출금액만큼 차감되고 저장된다") {
                result.balance shouldBe BigDecimal("700")
                verify { saveAccountGateway.saveAccount(account) }
            }
        }

        When("이체를 요청하면") {
            val fromAccount = Account("1", BigDecimal("1000"))
            val toAccount = Account("2", BigDecimal("500"))
            every { loadAccountGateway.loadAccount("1") } returns fromAccount
            every { loadAccountGateway.loadAccount("2") } returns toAccount
            every { saveAccountGateway.saveAccount(fromAccount) } returns fromAccount
            every { saveAccountGateway.saveAccount(toAccount) } returns toAccount

            val result = accountInteractor.transfer(TransferRequestModel("1", "2", BigDecimal("400")))

            Then("두 계좌의 잔액이 올바르게 갱신되어 영속화된다") {
                fromAccount.balance shouldBe BigDecimal("600")
                toAccount.balance shouldBe BigDecimal("900")
                result.balance shouldBe BigDecimal("900")
                verify { saveAccountGateway.saveAccount(fromAccount) }
                verify { saveAccountGateway.saveAccount(toAccount) }
            }
        }

        When("잔액을 초과하여 출금을 요청하면") {
            val account = Account("1", BigDecimal("1000"))
            every { loadAccountGateway.loadAccount("1") } returns account

            Then("Insufficient funds 예외가 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    accountInteractor.withdraw(WithdrawRequestModel("1", BigDecimal("1500")))
                }
                exception.message shouldBe "Insufficient funds"
            }
        }
    }
})

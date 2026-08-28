package jhkim105.tutorials.hexagonal.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jhkim105.tutorials.hexagonal.application.domain.model.Account
import jhkim105.tutorials.hexagonal.application.domain.service.AccountTransferDomainService
import jhkim105.tutorials.hexagonal.application.port.`in`.*
import jhkim105.tutorials.hexagonal.application.port.out.LoadAccountPort
import jhkim105.tutorials.hexagonal.application.port.out.SaveAccountPort
import java.math.BigDecimal

class AccountServiceTest : BehaviorSpec({
    val loadAccountPort = mockk<LoadAccountPort>()
    val saveAccountPort = mockk<SaveAccountPort>()
    val accountTransferDomainService = AccountTransferDomainService()
    val accountService = AccountService(loadAccountPort, saveAccountPort, accountTransferDomainService)

    Given("계좌 서비스 유스케이스가 준비되었을 때") {
        When("신규 계좌를 생성하면") {
            every { saveAccountPort.saveAccount(any()) } answers { firstArg() }

            val created = accountService.create(CreateAccountCommand(BigDecimal("500")))

            Then("초기 잔액을 가진 계좌가 정상 저장된다") {
                created.balance shouldBe BigDecimal("500")
                verify { saveAccountPort.saveAccount(any()) }
            }
        }

        When("입금을 수행하면") {
            val account = Account("1", BigDecimal("1000"))
            every { loadAccountPort.loadAccount("1") } returns account
            every { saveAccountPort.saveAccount(account) } returns account

            val result = accountService.deposit(DepositCommand("1", BigDecimal("200")))

            Then("잔액이 증가하고 저장된다") {
                result.balance shouldBe BigDecimal("1200")
                verify { saveAccountPort.saveAccount(account) }
            }
        }

        When("출금을 수행하면") {
            val account = Account("1", BigDecimal("1000"))
            every { loadAccountPort.loadAccount("1") } returns account
            every { saveAccountPort.saveAccount(account) } returns account

            val result = accountService.withdraw(WithdrawCommand("1", BigDecimal("300")))

            Then("잔액이 차감되고 저장된다") {
                result.balance shouldBe BigDecimal("700")
                verify { saveAccountPort.saveAccount(account) }
            }
        }

        When("이체를 수행하면") {
            val fromAccount = Account("1", BigDecimal("1000"))
            val toAccount = Account("2", BigDecimal("500"))
            every { loadAccountPort.loadAccount("1") } returns fromAccount
            every { loadAccountPort.loadAccount("2") } returns toAccount
            every { saveAccountPort.saveAccount(fromAccount) } returns fromAccount
            every { saveAccountPort.saveAccount(toAccount) } returns toAccount

            val result = accountService.transfer(TransferCommand("1", "2", BigDecimal("400")))

            Then("보낸 계좌와 받는 계좌 모두 잔액이 올바르게 갱신된다") {
                fromAccount.balance shouldBe BigDecimal("600")
                toAccount.balance shouldBe BigDecimal("900")
                result.balance shouldBe BigDecimal("900")
                verify { saveAccountPort.saveAccount(fromAccount) }
                verify { saveAccountPort.saveAccount(toAccount) }
            }
        }

        When("잔액을 초과하여 출금하면") {
            val account = Account("1", BigDecimal("1000"))
            every { loadAccountPort.loadAccount("1") } returns account

            Then("Insufficient funds 예외가 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    accountService.withdraw(WithdrawCommand("1", BigDecimal("1500")))
                }
                exception.message shouldBe "Insufficient funds"
            }
        }
    }
})

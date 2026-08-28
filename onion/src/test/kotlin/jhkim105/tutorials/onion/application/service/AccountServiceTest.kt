package jhkim105.tutorials.onion.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jhkim105.tutorials.onion.application.dto.*
import jhkim105.tutorials.onion.domain.model.Account
import jhkim105.tutorials.onion.domain.repository.AccountRepository
import jhkim105.tutorials.onion.domain.service.AccountTransferDomainService
import java.math.BigDecimal

class AccountServiceTest : BehaviorSpec({
    val accountRepository = mockk<AccountRepository>()
    val accountTransferDomainService = AccountTransferDomainService()
    val accountService = AccountService(accountRepository, accountTransferDomainService)

    Given("AccountService 유스케이스가 준비되었을 때") {
        When("신규 계좌 생성을 요청하면") {
            every { accountRepository.save(any()) } answers { firstArg() }

            val created = accountService.create(CreateAccountCommand(BigDecimal("500")))

            Then("초기 잔액을 가진 계좌가 정상 저장된다") {
                created.balance shouldBe BigDecimal("500")
                verify { accountRepository.save(any()) }
            }
        }

        When("입금을 요청하면") {
            val account = Account("1", BigDecimal("1000"))
            every { accountRepository.findById("1") } returns account
            every { accountRepository.save(account) } returns account

            val result = accountService.deposit(DepositCommand("1", BigDecimal("200")))

            Then("잔액이 입금액만큼 증가하고 저장된다") {
                result.balance shouldBe BigDecimal("1200")
                verify { accountRepository.save(account) }
            }
        }

        When("출금을 요청하면") {
            val account = Account("1", BigDecimal("1000"))
            every { accountRepository.findById("1") } returns account
            every { accountRepository.save(account) } returns account

            val result = accountService.withdraw(WithdrawCommand("1", BigDecimal("300")))

            Then("잔액이 출금액만큼 차감되고 저장된다") {
                result.balance shouldBe BigDecimal("700")
                verify { accountRepository.save(account) }
            }
        }

        When("이체를 요청하면") {
            val fromAccount = Account("1", BigDecimal("1000"))
            val toAccount = Account("2", BigDecimal("500"))
            every { accountRepository.findById("1") } returns fromAccount
            every { accountRepository.findById("2") } returns toAccount
            every { accountRepository.save(fromAccount) } returns fromAccount
            every { accountRepository.save(toAccount) } returns toAccount

            val result = accountService.transfer(TransferCommand("1", "2", BigDecimal("400")))

            Then("두 계좌의 잔액이 올바르게 갱신되어 영속화된다") {
                fromAccount.balance shouldBe BigDecimal("600")
                toAccount.balance shouldBe BigDecimal("900")
                result.balance shouldBe BigDecimal("900")
                verify { accountRepository.save(fromAccount) }
                verify { accountRepository.save(toAccount) }
            }
        }

        When("잔액을 초과하여 출금을 요청하면") {
            val account = Account("1", BigDecimal("1000"))
            every { accountRepository.findById("1") } returns account

            Then("Insufficient funds 예외가 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    accountService.withdraw(WithdrawCommand("1", BigDecimal("1500")))
                }
                exception.message shouldBe "Insufficient funds"
            }
        }
    }
})

package jhkim105.tutorials.spring.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jhkim105.tutorials.spring.model.Account
import jhkim105.tutorials.spring.repository.AccountRepository
import jhkim105.tutorials.spring.service.impl.AccountServiceImpl
import org.springframework.data.repository.findByIdOrNull
import java.math.BigDecimal

class AccountServiceTest : BehaviorSpec({
    val accountRepository = mockk<AccountRepository>()
    val accountService = AccountServiceImpl(accountRepository)

    Given("전통적인 Spring 계층형 서비스 테스트 환경이 주어졌을 때") {
        When("신규 계좌를 생성하면") {
            every { accountRepository.save(any()) } answers { firstArg() }

            val created = accountService.create(BigDecimal("500"))

            Then("JPA Repository의 save가 호출되고 계좌가 생성된다") {
                created.balance shouldBe BigDecimal("500")
                verify { accountRepository.save(any()) }
            }
        }

        When("입금을 요청하면") {
            val account = Account("1", BigDecimal("1000"))
            every { accountRepository.findByIdOrNull("1") } returns account
            every { accountRepository.save(account) } returns account

            val result = accountService.deposit("1", BigDecimal("200"))

            Then("엔티티의 잔액이 변경되고 JPA save가 호출된다") {
                result.balance shouldBe BigDecimal("1200")
                verify { accountRepository.save(account) }
            }
        }

        When("출금을 요청하면") {
            val account = Account("1", BigDecimal("1000"))
            every { accountRepository.findByIdOrNull("1") } returns account
            every { accountRepository.save(account) } returns account

            val result = accountService.withdraw("1", BigDecimal("300"))

            Then("잔액이 차감된다") {
                result.balance shouldBe BigDecimal("700")
                verify { accountRepository.save(account) }
            }
        }

        When("이체를 요청하면") {
            val fromAccount = Account("1", BigDecimal("1000"))
            val toAccount = Account("2", BigDecimal("500"))
            every { accountRepository.findByIdOrNull("1") } returns fromAccount
            every { accountRepository.findByIdOrNull("2") } returns toAccount
            every { accountRepository.save(fromAccount) } returns fromAccount
            every { accountRepository.save(toAccount) } returns toAccount

            val result = accountService.transfer("1", "2", BigDecimal("400"))

            Then("두 JPA 엔티티가 모두 갱신되어 저장된다") {
                fromAccount.balance shouldBe BigDecimal("600")
                toAccount.balance shouldBe BigDecimal("900")
                result.balance shouldBe BigDecimal("900")
                verify { accountRepository.save(fromAccount) }
                verify { accountRepository.save(toAccount) }
            }
        }

        When("잔액을 초과하여 출금하려고 하면") {
            val account = Account("1", BigDecimal("1000"))
            every { accountRepository.findByIdOrNull("1") } returns account

            Then("Insufficient funds 예외가 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    accountService.withdraw("1", BigDecimal("1500"))
                }
                exception.message shouldBe "Insufficient funds"
            }
        }
    }
})

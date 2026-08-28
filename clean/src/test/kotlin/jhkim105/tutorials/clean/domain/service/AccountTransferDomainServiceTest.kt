package jhkim105.tutorials.clean.domain.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import jhkim105.tutorials.clean.domain.entity.Account
import java.math.BigDecimal

class AccountTransferDomainServiceTest : BehaviorSpec({
    val domainService = AccountTransferDomainService()

    Given("두 개의 계좌가 존재할 때") {
        When("정상적인 금액을 이체하면") {
            val fromAccount = Account("1", BigDecimal("1000"))
            val toAccount = Account("2", BigDecimal("500"))

            domainService.transfer(fromAccount, toAccount, BigDecimal("400"))

            Then("출금 계좌 잔액은 차감되고 입금 계좌 잔액은 증가해야 한다") {
                fromAccount.balance shouldBe BigDecimal("600")
                toAccount.balance shouldBe BigDecimal("900")
            }
        }

        When("동일한 계좌로 이체를 시도하면") {
            val sameAccount = Account("1", BigDecimal("1000"))

            Then("동일 계좌 이체 불가 예외가 발생해야 한다") {
                val ex = shouldThrow<IllegalArgumentException> {
                    domainService.transfer(sameAccount, sameAccount, BigDecimal("100"))
                }
                ex.message shouldBe "Cannot transfer to the same account (1)"
            }
        }

        When("1회 최대 이체 한도를 초과하여 이체를 시도하면") {
            val fromAccount = Account("1", BigDecimal("20000000"))
            val toAccount = Account("2", BigDecimal("500"))
            val overLimitAmount = BigDecimal("10000001")

            Then("최대 이체 한도 초과 예외가 발생해야 한다") {
                val ex = shouldThrow<IllegalArgumentException> {
                    domainService.transfer(fromAccount, toAccount, overLimitAmount)
                }
                ex.message shouldBe "Transfer amount exceeds maximum limit of 10000000"
            }
        }

        When("잔액을 초과하여 이체를 시도하면") {
            val fromAccount = Account("1", BigDecimal("500"))
            val toAccount = Account("2", BigDecimal("500"))

            Then("잔액 부족 예외가 발생해야 한다") {
                val ex = shouldThrow<IllegalArgumentException> {
                    domainService.transfer(fromAccount, toAccount, BigDecimal("600"))
                }
                ex.message shouldBe "Insufficient funds"
            }
        }
    }
})

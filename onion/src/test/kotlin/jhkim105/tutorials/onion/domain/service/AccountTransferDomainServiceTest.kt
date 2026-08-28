package jhkim105.tutorials.onion.domain.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import jhkim105.tutorials.onion.domain.model.Account
import java.math.BigDecimal

class AccountTransferDomainServiceTest : BehaviorSpec({
    val domainService = AccountTransferDomainService()

    Given("두 개의 계좌가 주어졌을 때") {
        When("정상적인 금액을 이체하면") {
            val fromAccount = Account("1", BigDecimal("1000"))
            val toAccount = Account("2", BigDecimal("500"))

            domainService.transfer(fromAccount, toAccount, BigDecimal("400"))

            Then("출금 계좌는 차감되고 입금 계좌는 증가해야 한다") {
                fromAccount.balance shouldBe BigDecimal("600")
                toAccount.balance shouldBe BigDecimal("900")
            }
        }

        When("동일한 계좌로 이체를 시도하면") {
            val account = Account("1", BigDecimal("1000"))

            Then("동일 계좌 이체 불가 예외가 발생해야 한다") {
                val ex = shouldThrow<IllegalArgumentException> {
                    domainService.transfer(account, account, BigDecimal("100"))
                }
                ex.message shouldBe "Cannot transfer to the same account (1)"
            }
        }

        When("최대 이체 한도를 초과하여 이체를 시도하면") {
            val fromAccount = Account("1", BigDecimal("20000000"))
            val toAccount = Account("2", BigDecimal("500"))
            val overLimit = BigDecimal("10000001")

            Then("한도 초과 예외가 발생해야 한다") {
                val ex = shouldThrow<IllegalArgumentException> {
                    domainService.transfer(fromAccount, toAccount, overLimit)
                }
                ex.message shouldBe "Transfer amount exceeds maximum limit of 10000000"
            }
        }
    }
})

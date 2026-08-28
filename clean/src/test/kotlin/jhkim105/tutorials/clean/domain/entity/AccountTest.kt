package jhkim105.tutorials.clean.domain.entity

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class AccountTest : BehaviorSpec({
    Given("계좌가 생성되었을 때") {
        val account = Account("1", BigDecimal("1000"))

        When("양수 금액을 입금하면") {
            account.deposit(BigDecimal("500"))

            Then("잔액이 입금액만큼 증가한다") {
                account.balance shouldBe BigDecimal("1500")
            }
        }

        When("잔액 이하의 금액을 출금하면") {
            account.withdraw(BigDecimal("300"))

            Then("잔액이 출금액만큼 감소한다") {
                account.balance shouldBe BigDecimal("1200")
            }
        }

        When("잔액을 초과하여 출금하려고 하면") {
            Then("Insufficient funds 예외가 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    account.withdraw(BigDecimal("2000"))
                }
                exception.message shouldBe "Insufficient funds"
            }
        }

        When("0 이하의 금액을 입금하려고 하면") {
            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    account.deposit(BigDecimal("-100"))
                }
            }
        }

        When("0 이하의 금액을 출금하려고 하면") {
            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    account.withdraw(BigDecimal("-100"))
                }
            }
        }
    }
})

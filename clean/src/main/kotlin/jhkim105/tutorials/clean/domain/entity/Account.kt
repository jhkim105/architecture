package jhkim105.tutorials.clean.domain.entity

import java.math.BigDecimal

/**
 * Enterprise Business Rules (Entity):
 * 프레임워크나 외부 세부사항에 독립적인 가장 핵심적인 도메인 엔티티입니다.
 */
class Account(
    val id: String,
    balance: BigDecimal = BigDecimal.ZERO
) {
    var balance: BigDecimal = balance
        private set

    fun deposit(amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "Deposit amount must be positive" }
        balance = balance.add(amount)
    }

    fun withdraw(amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "Withdraw amount must be positive" }
        require(amount <= balance) { "Insufficient funds" }
        balance = balance.subtract(amount)
    }
}

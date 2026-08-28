package jhkim105.tutorials.onion.domain.model

import java.math.BigDecimal

/**
 * 1. Domain Model (Inner Core):
 * 프레임워크나 외부 계층에 전혀 의존하지 않는 가장 중심적인 도메인 엔티티입니다.
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

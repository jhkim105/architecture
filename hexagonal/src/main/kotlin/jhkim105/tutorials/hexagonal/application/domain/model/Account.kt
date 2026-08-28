package jhkim105.tutorials.hexagonal.application.domain.model

import java.math.BigDecimal

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
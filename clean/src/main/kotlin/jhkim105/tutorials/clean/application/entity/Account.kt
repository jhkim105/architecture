package jhkim105.tutorials.clean.application.entity

import java.math.BigDecimal

class Account(
    var id: String,
    var balance: BigDecimal
) {

    fun deposit(amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "Deposit amount must be positive" }
        balance = balance.add(amount)
    }

    fun withdraw(amount: BigDecimal) {
        require(amount <= balance) { "Insufficient funds" }
        balance = balance.subtract(amount)
    }

    fun transfer(targetAccount: Account, amount: BigDecimal) {
        this.withdraw(amount)
        targetAccount.deposit(amount)
    }

}
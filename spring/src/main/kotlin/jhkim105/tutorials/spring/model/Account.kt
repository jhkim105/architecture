package jhkim105.tutorials.spring.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.math.BigDecimal

@Entity
class Account(
    @Id
    val id: String,
    var balance: BigDecimal
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Account) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

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
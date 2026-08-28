package jhkim105.tutorials.spring.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

/**
 * Domain Model 겸 JPA Entity:
 * 전통적인 Spring 계층형 아키텍처에서는 별도의 도메인 모델과 영속성 엔티티를 분리하지 않고
 * 하나의 @Entity 클래스에 도메인 비즈니스 로직과 데이터베이스 매핑 설정을 함께 둡니다.
 */
@Entity
@Table(name = "account")
class Account(
    @Id
    val id: String,
    var balance: BigDecimal
) {

    fun deposit(amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "Deposit amount must be positive" }
        balance = balance.add(amount)
    }

    fun withdraw(amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "Withdraw amount must be positive" }
        require(amount <= balance) { "Insufficient funds" }
        balance = balance.subtract(amount)
    }

    fun transfer(targetAccount: Account, amount: BigDecimal) {
        this.withdraw(amount)
        targetAccount.deposit(amount)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Account) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

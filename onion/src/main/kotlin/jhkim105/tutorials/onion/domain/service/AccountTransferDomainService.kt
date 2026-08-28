package jhkim105.tutorials.onion.domain.service

import jhkim105.tutorials.onion.domain.model.Account
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * 2. Domain Services Layer:
 * 2개 이상의 엔티티(Account) 간의 상호작용 및 비즈니스 정책(동일 계좌 이체 불가, 최대 한도 검증)을 순수 비즈니스 로직으로 수행합니다.
 */
@Component
class AccountTransferDomainService {

    companion object {
        val MAX_TRANSFER_AMOUNT: BigDecimal = BigDecimal("10000000") // 1회 최대 1,000만원
    }

    fun transfer(fromAccount: Account, toAccount: Account, amount: BigDecimal) {
        require(fromAccount.id != toAccount.id) { "Cannot transfer to the same account (${fromAccount.id})" }
        require(amount > BigDecimal.ZERO) { "Transfer amount must be positive" }
        require(amount <= MAX_TRANSFER_AMOUNT) { "Transfer amount exceeds maximum limit of $MAX_TRANSFER_AMOUNT" }

        fromAccount.withdraw(amount)
        toAccount.deposit(amount)
    }
}

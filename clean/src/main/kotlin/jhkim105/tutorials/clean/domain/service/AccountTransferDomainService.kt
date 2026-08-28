package jhkim105.tutorials.clean.domain.service

import jhkim105.tutorials.clean.domain.entity.Account
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * Domain Service:
 * 여러 엔티티(Account) 간의 상호작용 및 도메인 정책(동일 계좌 검증, 최대 이체 한도)을 순수 비즈니스 로직으로 수행합니다.
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

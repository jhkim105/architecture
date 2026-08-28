package jhkim105.tutorials.hexagonal.application.domain.service

import jhkim105.tutorials.hexagonal.application.domain.model.Account
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * 도메인 서비스: 두 개 이상의 계좌(Aggregate) 간의 상호작용 및 비즈니스 정책(수수료, 이체 한도, 동일 계좌 검증)을 처리합니다.
 * 영속성이나 외부 I/O를 직접 다루지 않고 순수한 도메인 객체들만을 대상으로 비즈니스 규칙을 실행합니다.
 */
@Component
class AccountTransferDomainService {

    companion object {
        val MAX_TRANSFER_AMOUNT: BigDecimal = BigDecimal("10000000") // 1회 최대 천만원
    }

    fun transfer(fromAccount: Account, toAccount: Account, amount: BigDecimal) {
        require(fromAccount.id != toAccount.id) { "Cannot transfer to the same account (${fromAccount.id})" }
        require(amount > BigDecimal.ZERO) { "Transfer amount must be positive" }
        require(amount <= MAX_TRANSFER_AMOUNT) { "Transfer amount exceeds maximum limit of $MAX_TRANSFER_AMOUNT" }

        // 순수 도메인 로직 실행
        fromAccount.withdraw(amount)
        toAccount.deposit(amount)
    }
}

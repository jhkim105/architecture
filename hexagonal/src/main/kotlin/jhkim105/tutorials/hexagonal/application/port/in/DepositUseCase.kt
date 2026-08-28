package jhkim105.tutorials.hexagonal.application.port.`in`

import jhkim105.tutorials.hexagonal.application.domain.model.Account
import java.math.BigDecimal

interface DepositUseCase {
    fun deposit(command: DepositCommand): Account
}

data class DepositCommand(
    val accountId: String,
    val amount: BigDecimal
)

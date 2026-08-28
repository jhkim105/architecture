package jhkim105.tutorials.hexagonal.application.port.`in`

import jhkim105.tutorials.hexagonal.application.domain.model.Account
import java.math.BigDecimal

interface WithdrawUseCase {
    fun withdraw(command: WithdrawCommand): Account
}

data class WithdrawCommand(
    val accountId: String,
    val amount: BigDecimal
)

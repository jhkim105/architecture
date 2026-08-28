package jhkim105.tutorials.hexagonal.application.port.`in`

import jhkim105.tutorials.hexagonal.application.domain.model.Account
import java.math.BigDecimal

interface TransferUseCase {
    fun transfer(command: TransferCommand): Account
}

data class TransferCommand(
    val fromAccountId: String,
    val toAccountId: String,
    val amount: BigDecimal
)

package jhkim105.tutorials.hexagonal.application.port.`in`

import jhkim105.tutorials.hexagonal.application.domain.model.Account
import java.math.BigDecimal

interface CreateAccountUseCase {
    fun create(command: CreateAccountCommand): Account
}

data class CreateAccountCommand(
    val initialBalance: BigDecimal
)

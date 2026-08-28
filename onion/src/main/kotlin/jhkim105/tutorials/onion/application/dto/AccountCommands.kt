package jhkim105.tutorials.onion.application.dto

import java.math.BigDecimal

data class CreateAccountCommand(
    val initialBalance: BigDecimal
)

data class DepositCommand(
    val accountId: String,
    val amount: BigDecimal
)

data class WithdrawCommand(
    val accountId: String,
    val amount: BigDecimal
)

data class TransferCommand(
    val fromAccountId: String,
    val toAccountId: String,
    val amount: BigDecimal
)

data class GetAccountQuery(
    val accountId: String
)

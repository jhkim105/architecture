package jhkim105.tutorials.clean.usecase.model

import java.math.BigDecimal

data class CreateAccountRequestModel(
    val initialBalance: BigDecimal
)

data class DepositRequestModel(
    val accountId: String,
    val amount: BigDecimal
)

data class WithdrawRequestModel(
    val accountId: String,
    val amount: BigDecimal
)

data class TransferRequestModel(
    val fromAccountId: String,
    val toAccountId: String,
    val amount: BigDecimal
)

data class GetAccountRequestModel(
    val accountId: String
)

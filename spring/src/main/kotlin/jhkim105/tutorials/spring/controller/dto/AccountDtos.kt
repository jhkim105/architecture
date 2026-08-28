package jhkim105.tutorials.spring.controller.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jhkim105.tutorials.spring.model.Account
import java.math.BigDecimal

data class CreateAccountRequest(
    @field:NotNull
    @field:DecimalMin(value = "0.0", inclusive = true, message = "Initial balance must be zero or positive")
    val initialBalance: BigDecimal = BigDecimal.ZERO
)

data class DepositRequest(
    @field:NotNull
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Deposit amount must be greater than zero")
    val amount: BigDecimal
)

data class WithdrawRequest(
    @field:NotNull
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Withdraw amount must be greater than zero")
    val amount: BigDecimal
)

data class TransferRequest(
    @field:NotBlank
    val toAccountId: String,
    @field:NotNull
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Transfer amount must be greater than zero")
    val amount: BigDecimal
)

data class AccountResponse(
    val id: String,
    val balance: BigDecimal
) {
    companion object {
        fun from(account: Account): AccountResponse = AccountResponse(
            id = account.id,
            balance = account.balance
        )
    }
}

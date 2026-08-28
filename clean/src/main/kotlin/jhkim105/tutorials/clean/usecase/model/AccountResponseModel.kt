package jhkim105.tutorials.clean.usecase.model

import jhkim105.tutorials.clean.domain.entity.Account
import java.math.BigDecimal

data class AccountResponseModel(
    val id: String,
    val balance: BigDecimal
) {
    companion object {
        fun from(account: Account): AccountResponseModel = AccountResponseModel(
            id = account.id,
            balance = account.balance
        )
    }
}

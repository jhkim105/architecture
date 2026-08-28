package jhkim105.tutorials.hexagonal.application.port.`in`

import jhkim105.tutorials.hexagonal.application.domain.model.Account

interface GetAccountUseCase {
    fun get(query: GetAccountQuery): Account
}

data class GetAccountQuery(
    val accountId: String
)

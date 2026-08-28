package jhkim105.tutorials.clean.usecase.gateway

import jhkim105.tutorials.clean.domain.entity.Account

interface AccountGateway : LoadAccountGateway, SaveAccountGateway {
    fun findById(accountId: String): Account? = loadAccount(accountId)
    fun save(account: Account): Account = saveAccount(account)
}

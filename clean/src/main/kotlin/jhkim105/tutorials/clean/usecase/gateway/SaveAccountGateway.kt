package jhkim105.tutorials.clean.usecase.gateway

import jhkim105.tutorials.clean.domain.entity.Account

interface SaveAccountGateway {
    fun saveAccount(account: Account): Account
}

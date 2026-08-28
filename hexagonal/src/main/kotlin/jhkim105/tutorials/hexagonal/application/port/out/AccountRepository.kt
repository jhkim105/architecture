package jhkim105.tutorials.hexagonal.application.port.out

import jhkim105.tutorials.hexagonal.application.domain.model.Account

interface AccountRepository : LoadAccountPort, SaveAccountPort {
    fun findById(accountId: String): Account? = loadAccount(accountId)
    fun save(account: Account): Account = saveAccount(account)
}
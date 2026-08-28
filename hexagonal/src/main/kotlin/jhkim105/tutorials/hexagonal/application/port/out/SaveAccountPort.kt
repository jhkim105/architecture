package jhkim105.tutorials.hexagonal.application.port.out

import jhkim105.tutorials.hexagonal.application.domain.model.Account

interface SaveAccountPort {
    fun saveAccount(account: Account): Account
}

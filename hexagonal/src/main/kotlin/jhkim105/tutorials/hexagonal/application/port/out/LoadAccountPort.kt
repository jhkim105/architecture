package jhkim105.tutorials.hexagonal.application.port.out

import jhkim105.tutorials.hexagonal.application.domain.model.Account

interface LoadAccountPort {
    fun loadAccount(accountId: String): Account?
}

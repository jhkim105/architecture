package jhkim105.tutorials.onion.domain.repository

import jhkim105.tutorials.application.domain.model.Account
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository {
    fun findById(accountId: String): Account
    fun save(account: Account): Account
}
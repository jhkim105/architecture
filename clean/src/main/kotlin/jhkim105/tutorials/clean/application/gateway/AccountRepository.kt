package jhkim105.tutorials.clean.application.gateway

import jhkim105.tutorials.clean.application.entity.Account
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository {
    fun findById(accountId: String): Account
    fun save(account: Account): Account
}
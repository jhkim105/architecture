package jhkim105.tutorials.hexagonal.application.port.out

import jhkim105.tutorials.hexagonal.application.domain.model.Account
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository {
    fun findById(accountId: String): Account
    fun save(account: Account): Account
}
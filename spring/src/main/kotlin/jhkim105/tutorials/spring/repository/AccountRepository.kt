package jhkim105.tutorials.spring.repository

import jhkim105.tutorials.spring.model.Account
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<Account, String> {
}
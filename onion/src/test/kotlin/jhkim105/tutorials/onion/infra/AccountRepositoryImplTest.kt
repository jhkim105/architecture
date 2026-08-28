package jhkim105.tutorials.onion.infra

import jhkim105.tutorials.onion.domain.repository.AccountRepository
import jhkim105.tutorials.onion.infra.persistence.AccountMapper
import jhkim105.tutorials.onion.infra.persistence.AccountRepositoryImpl
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import java.math.BigDecimal
import kotlin.test.Test


@DataJpaTest
@Import(AccountRepositoryImpl::class, AccountMapper::class)
class AccountRepositoryImplTest {

    @Autowired
    lateinit var accountRepository: AccountRepository


    @Test
    @Sql("/sql/AccountRepositoryTest.sql")
    fun findById() {
        assertThat(accountRepository.findById("tid01")?.balance).isEqualTo(BigDecimal("100.00"))
    }

}
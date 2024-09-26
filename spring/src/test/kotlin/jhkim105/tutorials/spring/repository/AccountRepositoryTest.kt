package jhkim105.tutorials.spring.repository

import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.jdbc.Sql
import java.math.BigDecimal
import kotlin.test.Test


@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    lateinit var accountRepository: AccountRepository


    @Test
    @Sql("/sql/AccountRepositoryTest.sql")
    fun findById() {
        assertThat(accountRepository.findByIdOrNull("tid01")?.balance).isEqualTo(BigDecimal("100.00"))
    }

}
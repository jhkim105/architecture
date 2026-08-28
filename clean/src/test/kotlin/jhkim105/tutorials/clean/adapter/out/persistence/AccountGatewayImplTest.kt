package jhkim105.tutorials.clean.adapter.out.persistence

import jhkim105.tutorials.clean.usecase.gateway.AccountGateway
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import java.math.BigDecimal

@DataJpaTest
@Import(AccountGatewayImpl::class, AccountMapper::class)
class AccountGatewayImplTest {

    @Autowired
    lateinit var accountGateway: AccountGateway

    @Test
    @Sql("/sql/AccountRepositoryTest.sql")
    fun findById() {
        assertThat(accountGateway.findById("tid01")?.balance).isEqualTo(BigDecimal("100.00"))
    }
}

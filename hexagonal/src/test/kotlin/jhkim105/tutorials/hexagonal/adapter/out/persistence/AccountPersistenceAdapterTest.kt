package jhkim105.tutorials.hexagonal.adapter.out.persistence

import jhkim105.tutorials.hexagonal.application.domain.model.Account
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import java.math.BigDecimal
import kotlin.test.Test

@DataJpaTest
@Import(AccountPersistenceAdapter::class, AccountMapper::class)
class AccountPersistenceAdapterTest {

    @Autowired
    private lateinit var accountPersistenceAdapter: AccountPersistenceAdapter

    @Test
    @Sql("/sql/AccountRepositoryTest.sql")
    fun `계좌 ID로 계좌 정보를 정상 조회한다`() {
        val account = accountPersistenceAdapter.loadAccount("tid01")
        assertThat(account).isNotNull
        assertThat(account?.balance).isEqualTo(BigDecimal("100.00"))
    }

    @Test
    fun `계좌를 정상 저장하고 조회한다`() {
        val newAccount = Account(id = "acc-new-01", balance = BigDecimal("500.00"))
        accountPersistenceAdapter.saveAccount(newAccount)

        val loadedAccount = accountPersistenceAdapter.loadAccount("acc-new-01")
        assertThat(loadedAccount).isNotNull
        assertThat(loadedAccount?.id).isEqualTo("acc-new-01")
        assertThat(loadedAccount?.balance).isEqualTo(BigDecimal("500.00"))
    }
}

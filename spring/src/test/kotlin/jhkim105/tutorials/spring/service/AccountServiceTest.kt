package jhkim105.tutorials.spring.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jhkim105.tutorials.spring.model.Account
import jhkim105.tutorials.spring.repository.AccountRepository
import jhkim105.tutorials.spring.service.impl.AccountServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.math.BigDecimal
import java.util.*

class AccountServiceTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var accountService: AccountService

    @BeforeEach
    fun setUp() {
        accountRepository = mockk<AccountRepository>()
        accountService = AccountServiceImpl(accountRepository)
    }

    @Test
    fun `should deposit amount to account`() {
        val account = Account("1", BigDecimal(1000))
        every { accountRepository.findByIdOrNull("1") } answers { account }
        every { accountRepository.save(account) } answers { account }

        accountService.deposit("1", BigDecimal(200))

        assertEquals(BigDecimal(1200), account.balance)
        verify { accountRepository.save(account) }
    }

    @Test
    fun `should withdraw amount from account`() {
        val account = Account("1", BigDecimal(1000))
        every { accountRepository.findByIdOrNull("1") } answers { account }
        every { accountRepository.save(account) } answers { account }

        accountService.withdraw("1", BigDecimal(300))

        assertEquals(BigDecimal(700), account.balance)
        verify { accountRepository.save(account) }
    }

    @Test
    fun `should transfer amount between accounts`() {
        val fromAccount = Account("1", BigDecimal(1000))
        val toAccount = Account("2", BigDecimal(500))
        every { accountRepository.findByIdOrNull("1") } answers { fromAccount }
        every { accountRepository.findByIdOrNull("2") } answers { toAccount }
        every { accountRepository.save(fromAccount) } answers { fromAccount }
        every { accountRepository.save(toAccount) } answers { toAccount }

        accountService.transfer("1", "2", BigDecimal(400))

        assertEquals(BigDecimal(600), fromAccount.balance)
        assertEquals(BigDecimal(900), toAccount.balance)

        verify { accountRepository.save(fromAccount) }
        verify { accountRepository.save(toAccount) }
    }

    @Test
    fun `should throw exception when withdrawing more than balance`() {
        val account = Account("1", BigDecimal(1000))
        every { accountRepository.findByIdOrNull("1") } answers { account }
        every { accountRepository.save(account) } answers { account }

        val exception = assertThrows(IllegalArgumentException::class.java) {
            accountService.withdraw("1", BigDecimal(1500))
        }

        assertEquals("Insufficient funds", exception.message)
    }
}
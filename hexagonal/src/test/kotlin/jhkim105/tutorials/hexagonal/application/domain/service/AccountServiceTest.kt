package jhkim105.tutorials.hexagonal.application.domain.service

import jhkim105.tutorials.hexagonal.application.domain.model.Account
import jhkim105.tutorials.hexagonal.application.port.`in`.AccountService
import jhkim105.tutorials.hexagonal.application.port.out.AccountRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import java.math.BigDecimal

class AccountServiceTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var accountService: AccountService

    @BeforeEach
    fun setUp() {
        // AccountRepository 를 모킹
        accountRepository = mock()
        // 모킹된 AccountRepository를 AccountService에 주입
        accountService = AccountServiceImpl(accountRepository)
    }

    @Test
    fun `should deposit amount to account`() {
        // Arrange
        val account = Account("1", BigDecimal(1000))
        whenever(accountRepository.findById("1")).thenReturn(account)

        // Act
        accountService.deposit("1", BigDecimal(200))

        // Assert
        assertEquals(BigDecimal(1200), account.balance)
        verify(accountRepository).save(account)
    }

    @Test
    fun `should withdraw amount from account`() {
        // Arrange
        val account = Account("1", BigDecimal(1000))
        whenever(accountRepository.findById("1")).thenReturn(account)

        // Act
        accountService.withdraw("1", BigDecimal(300))

        // Assert
        assertEquals(BigDecimal(700), account.balance)
        verify(accountRepository).save(account)
    }

    @Test
    fun `should transfer amount between accounts`() {
        // Arrange
        val fromAccount = Account("1", BigDecimal(1000))
        val toAccount = Account("2", BigDecimal(500))
        whenever(accountRepository.findById("1")).thenReturn(fromAccount)
        whenever(accountRepository.findById("2")).thenReturn(toAccount)

        // Act
        accountService.transfer("1", "2", BigDecimal(400))

        // Assert
        assertEquals(BigDecimal(600), fromAccount.balance)
        assertEquals(BigDecimal(900), toAccount.balance)
        verify(accountRepository).save(fromAccount)
        verify(accountRepository).save(toAccount)
    }

    @Test
    fun `should throw exception when withdrawing more than balance`() {
        // Arrange
        val account = Account("1", BigDecimal(1000))
        whenever(accountRepository.findById("1")).thenReturn(account)

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            accountService.withdraw("1", BigDecimal(1500))
        }

        assertEquals("Insufficient funds", exception.message)
    }
}
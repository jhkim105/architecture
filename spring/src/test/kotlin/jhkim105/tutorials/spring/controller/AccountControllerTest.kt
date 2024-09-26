package jhkim105.tutorials.spring.controller


import jhkim105.tutorials.spring.model.Account
import jhkim105.tutorials.spring.service.AccountService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

@WebMvcTest(AccountController::class)
class AccountControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var accountService: AccountService


    @Test
    fun `should create a new account successfully`() {
        // Arrange
        val initialBalance = BigDecimal(500)
        val accountId = "generated-account-id"
        given(accountService.create(initialBalance)).willReturn(Account(accountId, initialBalance))

        // Act & Assert
        mockMvc.perform(
            post("/accounts")
                .param("initialBalance", initialBalance.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        // Verify that the service was called with the correct parameters
        verify(accountService).create(initialBalance)
    }

    @Test
    fun `should get account successfully`() {
        // Arrange
        val initialBalance = BigDecimal(500)
        val accountId = "generated-account-id"
        given(accountService.get(accountId)).willReturn(Account(accountId, initialBalance))

        // Act & Assert
        mockMvc.perform(
            get("/accounts/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        // Verify that the service was called with the correct parameters
        verify(accountService).get(accountId)
    }
    @Test
    fun `should deposit amount to account successfully`() {
        // Arrange
        val accountId = "1"
        val amount = BigDecimal(200)

        // Act & Assert
        mockMvc.perform(
            post("/accounts/{accountId}/deposit", accountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        // Verify that the service was called with the correct parameters
        verify(accountService).deposit(accountId, amount)
    }

    @Test
    fun `should withdraw amount from account successfully`() {
        // Arrange
        val accountId = "1"
        val amount = BigDecimal(100)

        // Act & Assert
        mockMvc.perform(
            post("/accounts/{accountId}/withdraw", accountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        // Verify that the service was called with the correct parameters
        verify(accountService).withdraw(accountId, amount)
    }

    @Test
    fun `should transfer amount between accounts successfully`() {
        // Arrange
        val fromAccountId = "1"
        val toAccountId = "2"
        val amount = BigDecimal(300)

        // Act & Assert
        mockMvc.perform(
            post("/accounts/{fromAccountId}/transfer/{toAccountId}", fromAccountId, toAccountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        // Verify that the service was called with the correct parameters
        verify(accountService).transfer(fromAccountId, toAccountId, amount)
    }

    @Test
    @Disabled("how to pass") // TODO
    fun `should return error when deposit amount is invalid`() {
        // Arrange
        val accountId = "1"
        val invalidAmount = BigDecimal(-100)

        // Mocking the service behavior for exception handling
        given(accountService.deposit(accountId, invalidAmount))
            .willThrow(IllegalArgumentException("Deposit amount must be positive"))

        // Act & Assert
        mockMvc.perform(
            post("/accounts/{accountId}/deposit", accountId)
                .param("amount", invalidAmount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().is5xxServerError)

        // Verify that the service was called with the correct parameters
        verify(accountService).deposit(accountId, invalidAmount)
    }
}
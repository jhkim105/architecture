package jhkim105.tutorials.onion.ui

import jhkim105.tutorials.onion.application.dto.*
import jhkim105.tutorials.onion.application.service.AccountService
import jhkim105.tutorials.onion.domain.model.Account
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
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
        val initialBalance = BigDecimal(500)
        val accountId = "generated-account-id"
        given(accountService.create(CreateAccountCommand(initialBalance)))
            .willReturn(Account(accountId, initialBalance))

        mockMvc.perform(
            post("/accounts")
                .param("initialBalance", initialBalance.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(accountId))
            .andExpect(jsonPath("$.balance").value(initialBalance))

        verify(accountService).create(CreateAccountCommand(initialBalance))
    }

    @Test
    fun `should get account successfully`() {
        val initialBalance = BigDecimal(500)
        val accountId = "generated-account-id"
        given(accountService.get(GetAccountQuery(accountId)))
            .willReturn(Account(accountId, initialBalance))

        mockMvc.perform(
            get("/accounts/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(accountId))
            .andExpect(jsonPath("$.balance").value(initialBalance))

        verify(accountService).get(GetAccountQuery(accountId))
    }

    @Test
    fun `should deposit amount to account successfully`() {
        val accountId = "1"
        val amount = BigDecimal(200)
        given(accountService.deposit(DepositCommand(accountId, amount)))
            .willReturn(Account(accountId, BigDecimal(1200)))

        mockMvc.perform(
            post("/accounts/{accountId}/deposit", accountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(BigDecimal(1200)))

        verify(accountService).deposit(DepositCommand(accountId, amount))
    }

    @Test
    fun `should withdraw amount from account successfully`() {
        val accountId = "1"
        val amount = BigDecimal(100)
        given(accountService.withdraw(WithdrawCommand(accountId, amount)))
            .willReturn(Account(accountId, BigDecimal(900)))

        mockMvc.perform(
            post("/accounts/{accountId}/withdraw", accountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(BigDecimal(900)))

        verify(accountService).withdraw(WithdrawCommand(accountId, amount))
    }

    @Test
    fun `should transfer amount between accounts successfully`() {
        val fromAccountId = "1"
        val toAccountId = "2"
        val amount = BigDecimal(300)
        given(accountService.transfer(TransferCommand(fromAccountId, toAccountId, amount)))
            .willReturn(Account(toAccountId, BigDecimal(800)))

        mockMvc.perform(
            post("/accounts/{fromAccountId}/transfer/{toAccountId}", fromAccountId, toAccountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(BigDecimal(800)))

        verify(accountService).transfer(TransferCommand(fromAccountId, toAccountId, amount))
    }
}

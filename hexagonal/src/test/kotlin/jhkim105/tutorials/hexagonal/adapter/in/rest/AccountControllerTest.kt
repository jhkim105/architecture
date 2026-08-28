package jhkim105.tutorials.hexagonal.adapter.`in`.rest

import io.mockk.every
import jhkim105.tutorials.hexagonal.application.domain.model.Account
import jhkim105.tutorials.hexagonal.application.port.`in`.*
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
    private lateinit var createAccountUseCase: CreateAccountUseCase

    @MockBean
    private lateinit var getAccountUseCase: GetAccountUseCase

    @MockBean
    private lateinit var depositUseCase: DepositUseCase

    @MockBean
    private lateinit var withdrawUseCase: WithdrawUseCase

    @MockBean
    private lateinit var transferUseCase: TransferUseCase

    @Test
    fun `should create a new account successfully`() {
        val initialBalance = BigDecimal(500)
        val accountId = "generated-account-id"
        given(createAccountUseCase.create(CreateAccountCommand(initialBalance)))
            .willReturn(Account(accountId, initialBalance))

        mockMvc.perform(
            post("/accounts")
                .param("initialBalance", initialBalance.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(accountId))
            .andExpect(jsonPath("$.balance").value(initialBalance))

        verify(createAccountUseCase).create(CreateAccountCommand(initialBalance))
    }

    @Test
    fun `should get account successfully`() {
        val initialBalance = BigDecimal(500)
        val accountId = "generated-account-id"
        given(getAccountUseCase.get(GetAccountQuery(accountId)))
            .willReturn(Account(accountId, initialBalance))

        mockMvc.perform(
            get("/accounts/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(accountId))
            .andExpect(jsonPath("$.balance").value(initialBalance))

        verify(getAccountUseCase).get(GetAccountQuery(accountId))
    }

    @Test
    fun `should deposit amount to account successfully`() {
        val accountId = "1"
        val amount = BigDecimal(200)
        given(depositUseCase.deposit(DepositCommand(accountId, amount)))
            .willReturn(Account(accountId, BigDecimal(1200)))

        mockMvc.perform(
            post("/accounts/{accountId}/deposit", accountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(BigDecimal(1200)))

        verify(depositUseCase).deposit(DepositCommand(accountId, amount))
    }

    @Test
    fun `should withdraw amount from account successfully`() {
        val accountId = "1"
        val amount = BigDecimal(100)
        given(withdrawUseCase.withdraw(WithdrawCommand(accountId, amount)))
            .willReturn(Account(accountId, BigDecimal(900)))

        mockMvc.perform(
            post("/accounts/{accountId}/withdraw", accountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(BigDecimal(900)))

        verify(withdrawUseCase).withdraw(WithdrawCommand(accountId, amount))
    }

    @Test
    fun `should transfer amount between accounts successfully`() {
        val fromAccountId = "1"
        val toAccountId = "2"
        val amount = BigDecimal(300)
        given(transferUseCase.transfer(TransferCommand(fromAccountId, toAccountId, amount)))
            .willReturn(Account(toAccountId, BigDecimal(800)))

        mockMvc.perform(
            post("/accounts/{fromAccountId}/transfer/{toAccountId}", fromAccountId, toAccountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(BigDecimal(800)))

        verify(transferUseCase).transfer(TransferCommand(fromAccountId, toAccountId, amount))
    }
}

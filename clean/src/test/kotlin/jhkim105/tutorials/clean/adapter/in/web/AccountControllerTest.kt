package jhkim105.tutorials.clean.adapter.`in`.web

import jhkim105.tutorials.clean.domain.entity.Account
import jhkim105.tutorials.clean.usecase.boundary.`in`.*
import jhkim105.tutorials.clean.usecase.model.*
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
    private lateinit var createAccountInputBoundary: CreateAccountInputBoundary

    @MockBean
    private lateinit var getAccountInputBoundary: GetAccountInputBoundary

    @MockBean
    private lateinit var depositInputBoundary: DepositInputBoundary

    @MockBean
    private lateinit var withdrawInputBoundary: WithdrawInputBoundary

    @MockBean
    private lateinit var transferInputBoundary: TransferInputBoundary

    @Test
    fun `should create a new account successfully`() {
        val initialBalance = BigDecimal(500)
        val accountId = "generated-account-id"
        given(createAccountInputBoundary.create(CreateAccountRequestModel(initialBalance)))
            .willReturn(Account(accountId, initialBalance))

        mockMvc.perform(
            post("/accounts")
                .param("initialBalance", initialBalance.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(accountId))
            .andExpect(jsonPath("$.balance").value(initialBalance))

        verify(createAccountInputBoundary).create(CreateAccountRequestModel(initialBalance))
    }

    @Test
    fun `should get account successfully`() {
        val initialBalance = BigDecimal(500)
        val accountId = "generated-account-id"
        given(getAccountInputBoundary.get(GetAccountRequestModel(accountId)))
            .willReturn(Account(accountId, initialBalance))

        mockMvc.perform(
            get("/accounts/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(accountId))
            .andExpect(jsonPath("$.balance").value(initialBalance))

        verify(getAccountInputBoundary).get(GetAccountRequestModel(accountId))
    }

    @Test
    fun `should deposit amount to account successfully`() {
        val accountId = "1"
        val amount = BigDecimal(200)
        given(depositInputBoundary.deposit(DepositRequestModel(accountId, amount)))
            .willReturn(Account(accountId, BigDecimal(1200)))

        mockMvc.perform(
            post("/accounts/{accountId}/deposit", accountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(BigDecimal(1200)))

        verify(depositInputBoundary).deposit(DepositRequestModel(accountId, amount))
    }

    @Test
    fun `should withdraw amount from account successfully`() {
        val accountId = "1"
        val amount = BigDecimal(100)
        given(withdrawInputBoundary.withdraw(WithdrawRequestModel(accountId, amount)))
            .willReturn(Account(accountId, BigDecimal(900)))

        mockMvc.perform(
            post("/accounts/{accountId}/withdraw", accountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(BigDecimal(900)))

        verify(withdrawInputBoundary).withdraw(WithdrawRequestModel(accountId, amount))
    }

    @Test
    fun `should transfer amount between accounts successfully`() {
        val fromAccountId = "1"
        val toAccountId = "2"
        val amount = BigDecimal(300)
        given(transferInputBoundary.transfer(TransferRequestModel(fromAccountId, toAccountId, amount)))
            .willReturn(Account(toAccountId, BigDecimal(800)))

        mockMvc.perform(
            post("/accounts/{fromAccountId}/transfer/{toAccountId}", fromAccountId, toAccountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(BigDecimal(800)))

        verify(transferInputBoundary).transfer(TransferRequestModel(fromAccountId, toAccountId, amount))
    }
}

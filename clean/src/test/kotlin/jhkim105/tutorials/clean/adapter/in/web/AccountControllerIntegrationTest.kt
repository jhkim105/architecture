package jhkim105.tutorials.clean.adapter.`in`.web

import com.fasterxml.jackson.databind.ObjectMapper
import jhkim105.tutorials.clean.adapter.`in`.web.dto.AccountResponse
import jhkim105.tutorials.clean.adapter.out.persistence.AccountJpaEntity
import jhkim105.tutorials.clean.adapter.out.persistence.AccountJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var accountJpaRepository: AccountJpaRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        accountJpaRepository.deleteAll()
        accountJpaRepository.save(AccountJpaEntity("1", BigDecimal(1000)))
        accountJpaRepository.save(AccountJpaEntity("2", BigDecimal(500)))
    }

    @Test
    fun `should create a new account successfully`() {
        val initialBalance = BigDecimal(300)

        val result = mockMvc.perform(
            post("/accounts")
                .param("initialBalance", initialBalance.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(initialBalance))
            .andReturn()

        val createdAccount = objectMapper.readValue(result.response.contentAsString, AccountResponse::class.java)

        val savedAccount = accountJpaRepository.findById(createdAccount.id)
        assertTrue(savedAccount.isPresent)
        assertEquals(initialBalance, savedAccount.get().balance)
    }

    @Test
    fun `should deposit amount to account successfully`() {
        val accountId = "1"
        val amount = BigDecimal(200)

        mockMvc.perform(
            post("/accounts/{accountId}/deposit", accountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        val updatedAccount = accountJpaRepository.findById(accountId).get()
        assertEquals(BigDecimal(1200), updatedAccount.balance)
    }

    @Test
    fun `should withdraw amount from account successfully`() {
        val accountId = "1"
        val amount = BigDecimal(100)

        mockMvc.perform(
            post("/accounts/{accountId}/withdraw", accountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        val updatedAccount = accountJpaRepository.findById(accountId).get()
        assertEquals(BigDecimal(900), updatedAccount.balance)
    }

    @Test
    fun `should transfer amount between accounts successfully`() {
        val fromAccountId = "1"
        val toAccountId = "2"
        val amount = BigDecimal(300)

        mockMvc.perform(
            post("/accounts/{fromAccountId}/transfer/{toAccountId}", fromAccountId, toAccountId)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        val updatedFromAccount = accountJpaRepository.findById(fromAccountId).get()
        val updatedToAccount = accountJpaRepository.findById(toAccountId).get()

        assertEquals(BigDecimal(700), updatedFromAccount.balance)
        assertEquals(BigDecimal(800), updatedToAccount.balance)
    }
}

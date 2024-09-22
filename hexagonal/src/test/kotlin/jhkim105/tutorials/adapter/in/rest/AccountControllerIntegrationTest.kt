package jhkim105.tutorials.adapter.`in`.rest

import com.fasterxml.jackson.databind.ObjectMapper
import jhkim105.tutorials.adapter.out.persistence.AccountJpaEntity
import jhkim105.tutorials.adapter.out.persistence.AccountJpaRepository
import jhkim105.tutorials.application.domain.model.Account
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.xmlunit.util.Mapper
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
        // 테스트용 데이터 초기화 (H2 인메모리 DB)
        accountJpaRepository.deleteAll()
        accountJpaRepository.save(AccountJpaEntity("1", BigDecimal(1000)))
        accountJpaRepository.save(AccountJpaEntity("2", BigDecimal(500)))
    }

    @Test
    fun `should create a new account successfully`() {
        // Arrange
        val initialBalance = BigDecimal(300)

        // Act & Assert
        val result = mockMvc.perform(
            post("/accounts")
                .param("initialBalance", initialBalance.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(initialBalance)) // 잔액 검증

            .andReturn()

        // Extract created account ID from the response
        val responseContent = result.response.contentAsString
        val createdAccount = objectMapper.readValue(result.response.contentAsString, Account::class.java)

        // Verify that the account was created in the database
        val savedAccount = accountJpaRepository.findById(createdAccount.id)
        assertTrue(savedAccount.isPresent)
        assertEquals(initialBalance, savedAccount.get().balance)
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
            .andExpect(content().string("Deposit successful"))

        // Verify the database state
        val updatedAccount = accountJpaRepository.findById(accountId).get()
        assertEquals(BigDecimal(1200), updatedAccount.balance)
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
            .andExpect(content().string("Withdraw successful"))

        // Verify the database state
        val updatedAccount = accountJpaRepository.findById(accountId).get()
        assertEquals(BigDecimal(900), updatedAccount.balance)
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

        // Verify the database state
        val updatedFromAccount = accountJpaRepository.findById(fromAccountId).get()
        val updatedToAccount = accountJpaRepository.findById(toAccountId).get()

        assertEquals(BigDecimal(700), updatedFromAccount.balance)
        assertEquals(BigDecimal(800), updatedToAccount.balance)
    }

    @Test
    fun `should return error when deposit amount is invalid`() {
        // Arrange
        val accountId = "1"
        val invalidAmount = BigDecimal(-100)

        // Act & Assert
        mockMvc.perform(
            post("/accounts/{accountId}/deposit", accountId)
                .param("amount", invalidAmount.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().string("Deposit amount must be positive"))
    }
}
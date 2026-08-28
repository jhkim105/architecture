package jhkim105.tutorials.spring.controller

import jakarta.validation.Valid
import jhkim105.tutorials.spring.controller.dto.*
import jhkim105.tutorials.spring.service.AccountService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * Controller Layer:
 * HTTP 요청을 받아 Service 계층을 호출하고 결과를 DTO로 변환하여 응답합니다.
 */
@RestController
@RequestMapping("/accounts")
class AccountController(
    private val accountService: AccountService
) {

    @PostMapping
    fun create(
        @RequestParam(required = false) initialBalance: BigDecimal?,
        @Valid @RequestBody(required = false) request: CreateAccountRequest?
    ): ResponseEntity<AccountResponse> {
        val balance = request?.initialBalance ?: initialBalance ?: BigDecimal.ZERO
        val account = accountService.create(balance)
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @GetMapping("/{accountId}")
    fun get(@PathVariable accountId: String): ResponseEntity<AccountResponse> {
        val account = accountService.get(accountId)
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @PostMapping("/{accountId}/deposit")
    fun deposit(
        @PathVariable accountId: String,
        @RequestParam(required = false) amount: BigDecimal?,
        @Valid @RequestBody(required = false) request: DepositRequest?
    ): ResponseEntity<AccountResponse> {
        val depositAmount = request?.amount ?: amount ?: throw IllegalArgumentException("amount is required")
        val account = accountService.deposit(accountId, depositAmount)
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @PostMapping("/{accountId}/withdraw")
    fun withdraw(
        @PathVariable accountId: String,
        @RequestParam(required = false) amount: BigDecimal?,
        @Valid @RequestBody(required = false) request: WithdrawRequest?
    ): ResponseEntity<AccountResponse> {
        val withdrawAmount = request?.amount ?: amount ?: throw IllegalArgumentException("amount is required")
        val account = accountService.withdraw(accountId, withdrawAmount)
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @PostMapping("/{fromAccountId}/transfer/{toAccountId}")
    fun transfer(
        @PathVariable fromAccountId: String,
        @PathVariable toAccountId: String,
        @RequestParam(required = false) amount: BigDecimal?,
        @Valid @RequestBody(required = false) request: TransferRequest?
    ): ResponseEntity<AccountResponse> {
        val transferAmount = request?.amount ?: amount ?: throw IllegalArgumentException("amount is required")
        val targetId = request?.toAccountId ?: toAccountId
        val account = accountService.transfer(fromAccountId, targetId, transferAmount)
        return ResponseEntity.ok(AccountResponse.from(account))
    }
}

package jhkim105.tutorials.onion.ui

import jakarta.validation.Valid
import jhkim105.tutorials.onion.application.dto.*
import jhkim105.tutorials.onion.application.service.AccountService
import jhkim105.tutorials.onion.ui.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * 4. Outer Ring: UI Layer (Controller):
 * 외부 HTTP 요청을 Application DTO(Command/Query)로 매핑하고, 결과를 클라이언트용 Web Response DTO로 반환합니다.
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
        val account = accountService.create(CreateAccountCommand(balance))
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @GetMapping("/{accountId}")
    fun get(@PathVariable accountId: String): ResponseEntity<AccountResponse> {
        val account = accountService.get(GetAccountQuery(accountId))
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @PostMapping("/{accountId}/deposit")
    fun deposit(
        @PathVariable accountId: String,
        @RequestParam(required = false) amount: BigDecimal?,
        @Valid @RequestBody(required = false) request: DepositRequest?
    ): ResponseEntity<AccountResponse> {
        val depositAmount = request?.amount ?: amount ?: throw IllegalArgumentException("amount is required")
        val account = accountService.deposit(DepositCommand(accountId, depositAmount))
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @PostMapping("/{accountId}/withdraw")
    fun withdraw(
        @PathVariable accountId: String,
        @RequestParam(required = false) amount: BigDecimal?,
        @Valid @RequestBody(required = false) request: WithdrawRequest?
    ): ResponseEntity<AccountResponse> {
        val withdrawAmount = request?.amount ?: amount ?: throw IllegalArgumentException("amount is required")
        val account = accountService.withdraw(WithdrawCommand(accountId, withdrawAmount))
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
        val account = accountService.transfer(TransferCommand(fromAccountId, targetId, transferAmount))
        return ResponseEntity.ok(AccountResponse.from(account))
    }
}

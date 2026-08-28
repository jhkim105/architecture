package jhkim105.tutorials.hexagonal.adapter.`in`.rest

import jakarta.validation.Valid
import jhkim105.tutorials.hexagonal.adapter.`in`.rest.dto.AccountResponse
import jhkim105.tutorials.hexagonal.adapter.`in`.rest.dto.CreateAccountRequest
import jhkim105.tutorials.hexagonal.adapter.`in`.rest.dto.DepositRequest
import jhkim105.tutorials.hexagonal.adapter.`in`.rest.dto.TransferRequest
import jhkim105.tutorials.hexagonal.adapter.`in`.rest.dto.WithdrawRequest
import jhkim105.tutorials.hexagonal.application.port.`in`.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/accounts")
class AccountController(
    private val createAccountUseCase: CreateAccountUseCase,
    private val getAccountUseCase: GetAccountUseCase,
    private val depositUseCase: DepositUseCase,
    private val withdrawUseCase: WithdrawUseCase,
    private val transferUseCase: TransferUseCase
) {

    @PostMapping
    fun create(
        @RequestParam(required = false) initialBalance: BigDecimal?,
        @RequestBody(required = false) request: CreateAccountRequest?
    ): ResponseEntity<AccountResponse> {
        val balance = request?.initialBalance ?: initialBalance ?: BigDecimal.ZERO
        val account = createAccountUseCase.create(CreateAccountCommand(balance))
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @GetMapping("/{accountId}")
    fun get(@PathVariable accountId: String): ResponseEntity<AccountResponse> {
        val account = getAccountUseCase.get(GetAccountQuery(accountId))
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @PostMapping("/{accountId}/deposit")
    fun deposit(
        @PathVariable accountId: String,
        @RequestParam(required = false) amount: BigDecimal?,
        @RequestBody(required = false) request: DepositRequest?
    ): ResponseEntity<AccountResponse> {
        val depositAmount = request?.amount ?: amount ?: throw IllegalArgumentException("amount is required")
        val account = depositUseCase.deposit(DepositCommand(accountId, depositAmount))
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @PostMapping("/{accountId}/withdraw")
    fun withdraw(
        @PathVariable accountId: String,
        @RequestParam(required = false) amount: BigDecimal?,
        @RequestBody(required = false) request: WithdrawRequest?
    ): ResponseEntity<AccountResponse> {
        val withdrawAmount = request?.amount ?: amount ?: throw IllegalArgumentException("amount is required")
        val account = withdrawUseCase.withdraw(WithdrawCommand(accountId, withdrawAmount))
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @PostMapping("/{fromAccountId}/transfer/{toAccountId}")
    fun transfer(
        @PathVariable fromAccountId: String,
        @PathVariable toAccountId: String,
        @RequestParam(required = false) amount: BigDecimal?,
        @RequestBody(required = false) request: TransferRequest?
    ): ResponseEntity<AccountResponse> {
        val transferAmount = request?.amount ?: amount ?: throw IllegalArgumentException("amount is required")
        val targetId = request?.toAccountId ?: toAccountId
        val account = transferUseCase.transfer(TransferCommand(fromAccountId, targetId, transferAmount))
        return ResponseEntity.ok(AccountResponse.from(account))
    }
}
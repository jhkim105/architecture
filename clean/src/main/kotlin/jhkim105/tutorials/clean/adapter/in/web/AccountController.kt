package jhkim105.tutorials.clean.adapter.`in`.web

import jakarta.validation.Valid
import jhkim105.tutorials.clean.adapter.`in`.web.dto.*
import jhkim105.tutorials.clean.usecase.boundary.`in`.*
import jhkim105.tutorials.clean.usecase.model.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * Interface Adapter (Web Controller):
 * 외부 HTTP 요청을 유스케이스 Input Boundary에 맞는 RequestModel로 변환하여 전달하고,
 * 유스케이스 결과를 클라이언트용 Web Response DTO로 매핑하여 반환합니다.
 */
@RestController
@RequestMapping("/accounts")
class AccountController(
    private val createAccountInputBoundary: CreateAccountInputBoundary,
    private val getAccountInputBoundary: GetAccountInputBoundary,
    private val depositInputBoundary: DepositInputBoundary,
    private val withdrawInputBoundary: WithdrawInputBoundary,
    private val transferInputBoundary: TransferInputBoundary
) {

    @PostMapping
    fun create(
        @RequestParam(required = false) initialBalance: BigDecimal?,
        @Valid @RequestBody(required = false) request: CreateAccountRequest?
    ): ResponseEntity<AccountResponse> {
        val balance = request?.initialBalance ?: initialBalance ?: BigDecimal.ZERO
        val account = createAccountInputBoundary.create(CreateAccountRequestModel(balance))
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @GetMapping("/{accountId}")
    fun get(@PathVariable accountId: String): ResponseEntity<AccountResponse> {
        val account = getAccountInputBoundary.get(GetAccountRequestModel(accountId))
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @PostMapping("/{accountId}/deposit")
    fun deposit(
        @PathVariable accountId: String,
        @RequestParam(required = false) amount: BigDecimal?,
        @Valid @RequestBody(required = false) request: DepositRequest?
    ): ResponseEntity<AccountResponse> {
        val depositAmount = request?.amount ?: amount ?: throw IllegalArgumentException("amount is required")
        val account = depositInputBoundary.deposit(DepositRequestModel(accountId, depositAmount))
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @PostMapping("/{accountId}/withdraw")
    fun withdraw(
        @PathVariable accountId: String,
        @RequestParam(required = false) amount: BigDecimal?,
        @Valid @RequestBody(required = false) request: WithdrawRequest?
    ): ResponseEntity<AccountResponse> {
        val withdrawAmount = request?.amount ?: amount ?: throw IllegalArgumentException("amount is required")
        val account = withdrawInputBoundary.withdraw(WithdrawRequestModel(accountId, withdrawAmount))
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
        val account = transferInputBoundary.transfer(TransferRequestModel(fromAccountId, targetId, transferAmount))
        return ResponseEntity.ok(AccountResponse.from(account))
    }
}

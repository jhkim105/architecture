package jhkim105.tutorials.onion.ui

import jhkim105.tutorials.onion.application.AccountService
import jhkim105.tutorials.onion.domain.model.Account
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/accounts")
class AccountController(
    private val accountService: AccountService
) {
    @PostMapping
    fun create(
        @RequestParam initialBalance: BigDecimal
    ): ResponseEntity<Account> {
        val account = accountService.create(initialBalance)
        return ResponseEntity.ok(account)
    }

    @GetMapping("/{accountId}")
    fun get( @PathVariable accountId: String): ResponseEntity<Account> {
        val account = accountService.get(accountId)
        return ResponseEntity.ok(account)
    }

    @PostMapping("/{accountId}/deposit")
    fun deposit(@PathVariable accountId: String, @RequestParam amount: BigDecimal) {
        accountService.deposit(accountId, amount)
    }

    @PostMapping("/{accountId}/withdraw")
    fun withdraw(@PathVariable accountId: String, @RequestParam amount: BigDecimal) {
        accountService.withdraw(accountId, amount)
    }

    @PostMapping("/{fromAccountId}/transfer/{toAccountId}")
    fun transfer(
        @PathVariable fromAccountId: String,
        @PathVariable toAccountId: String,
        @RequestParam amount: BigDecimal
    ) {
        accountService.transfer(fromAccountId, toAccountId, amount)
    }
}
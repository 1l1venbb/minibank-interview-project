package ro.axonsoft.eval.minibank.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.axonsoft.eval.minibank.dto.AccountResponse;
import ro.axonsoft.eval.minibank.dto.CreateAccountRequest;
import ro.axonsoft.eval.minibank.dto.PagedResponse;
import ro.axonsoft.eval.minibank.dto.TransactionResponse;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(
                request.ownerName(),
                request.iban(),
                request.currency(),
                request.accountType()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toAccountResponse(account));
    }

    @GetMapping("/{accountId}")
    public AccountResponse getAccountById(@PathVariable Long accountId) {
        return toAccountResponse(accountService.getAccountById(accountId));
    }

    @GetMapping
    public PagedResponse<AccountResponse> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AccountResponse> accounts = accountService.getAllAccounts(page, size).map(this::toAccountResponse);
        return PagedResponse.from(accounts);
    }

    @GetMapping("/{accountId}/transactions")
    public PagedResponse<TransactionResponse> getAccountTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return PagedResponse.from(accountService.getAccountTransactions(accountId, page, size));
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOwnerName(),
                account.getIban(),
                account.getCurrency(),
                account.getType().name(),
                account.getBalance(),
                account.getCreatedAt().toString()
        );
    }
}

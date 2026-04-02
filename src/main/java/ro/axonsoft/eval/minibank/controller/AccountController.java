package ro.axonsoft.eval.minibank.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.axonsoft.eval.minibank.dto.CreateAccountRequest;
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
    public ResponseEntity<Account> createAccount(@Valid @RequestBody CreateAccountRequest request) {

        Account account = accountService.createAccount(
                request.ownerName(), request.iban(), request.currency(), request.type()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{accountId}")
    public Account getAccountById(@PathVariable Long accountId) {
        return accountService.getAccountById(accountId);
    }

    @GetMapping
    public Page<Account> getAllAccounts(@RequestParam(defaultValue = "0")  int page,@RequestParam(defaultValue = "10") int size) {
        return accountService.getAllAccounts(page, size);
    }


}

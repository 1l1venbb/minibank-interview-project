package ro.axonsoft.eval.minibank.service;

import ro.axonsoft.eval.minibank.repository.AccountRepository;

public class AccountService {
    private final AccountRepository accountRepository;
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


}

package ro.axonsoft.eval.minibank.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ro.axonsoft.eval.minibank.exception.AccountAlreadyExists;
import ro.axonsoft.eval.minibank.exception.AccountNotFound;
import ro.axonsoft.eval.minibank.exception.InvalidAccountType;
import ro.axonsoft.eval.minibank.exception.UnsupportedCurrency;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.repository.AccountRepository;

import java.util.Locale;
import java.util.Set;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    private static final Set<String> SUPPORTED_CURRENCIES =
            Set.of("RON", "EUR", "USD", "GBP");



    public Account createAccount(String ownerName, String iban, String currency, String type) {

        if (accountRepository.findByIban(iban).isPresent())
            throw new AccountAlreadyExists(iban);

        AccountType accType;
        try{
            accType = AccountType.valueOf(type.toUpperCase());
        }catch (IllegalArgumentException e){
            throw new InvalidAccountType(type);
        }

        String curr = currency.toUpperCase(Locale.ROOT);
        if (!SUPPORTED_CURRENCIES.contains(curr)) {
            throw new UnsupportedCurrency(curr);
        }

        Account account = new Account(ownerName, iban, curr, accType);

        return accountRepository.save(account);

    }

    public Page<Account> getAllAccounts(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        return accountRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFound(accountId)) ;
    }

}

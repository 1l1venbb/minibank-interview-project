package ro.axonsoft.eval.minibank.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.axonsoft.eval.minibank.exception.AccountAlreadyExists;
import ro.axonsoft.eval.minibank.exception.AccountNotFound;
import ro.axonsoft.eval.minibank.exception.InvalidAccountType;
import ro.axonsoft.eval.minibank.dto.TransactionResponse;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.model.Transaction;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.repository.TransactionRepository;
import ro.axonsoft.eval.minibank.util.IbanUtils;

import java.util.Locale;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;

    public AccountService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            ExchangeRateService exchangeRateService
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.exchangeRateService = exchangeRateService;
    }

    public Account createAccount(String ownerName, String iban, String currency, String accountType) {
        String normalizedIban = IbanUtils.requireValid(iban);

        if (accountRepository.existsByIban(normalizedIban))
            throw new AccountAlreadyExists(normalizedIban);

        AccountType accType;
        try{
            accType = AccountType.valueOf(accountType.toUpperCase(Locale.ROOT));
        }catch (IllegalArgumentException e){
            throw new InvalidAccountType(accountType);
        }

        String curr = exchangeRateService.requireSupportedCurrency(currency);

        Account account = new Account(ownerName, normalizedIban, curr, accType);

        try {
            return accountRepository.save(account);
        } catch (DataIntegrityViolationException e) {
            throw new AccountAlreadyExists(normalizedIban);
        }

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

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAccountTransactions(Long accountId, int page, int size) {
        getAccountById(accountId);
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("timestamp").ascending().and(Sort.by("id").ascending()));
        return transactionRepository.findByAccount_Id(accountId, pageRequest)
                .map(this::toTransactionResponse);
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTimestamp().toString(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getBalanceAfter(),
                transaction.getCounterpartyIban(),
                transaction.getTransfer().getId()
        );
    }

}

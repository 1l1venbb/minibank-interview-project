package ro.axonsoft.eval.minibank.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ro.axonsoft.eval.minibank.dto.CreateTransferRequest;
import ro.axonsoft.eval.minibank.dto.TransferResponse;
import ro.axonsoft.eval.minibank.exception.AccountNotFound;
import ro.axonsoft.eval.minibank.exception.InsufficientFunds;
import ro.axonsoft.eval.minibank.exception.InvalidTransferAmount;
import ro.axonsoft.eval.minibank.exception.SameAccountTransfer;
import ro.axonsoft.eval.minibank.exception.SavingsDailyLimitExceeded;
import ro.axonsoft.eval.minibank.exception.SepaTransferNotAllowed;
import ro.axonsoft.eval.minibank.exception.TransferNotFound;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.model.Transaction;
import ro.axonsoft.eval.minibank.model.TransactionType;
import ro.axonsoft.eval.minibank.model.Transfer;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.repository.TransactionRepository;
import ro.axonsoft.eval.minibank.repository.TransferRepository;
import ro.axonsoft.eval.minibank.util.IbanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TransferService {
    private static final String BANK_IBAN = "RO49AAAA1B31007593840000";
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal SAVINGS_DAILY_LIMIT_EUR = new BigDecimal("5000.00");
    private static final int MONEY_SCALE = 2;
    private static final Set<String> SEPA_IBAN_COUNTRY_CODES = Set.of(
            "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR",
            "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL",
            "PL", "PT", "RO", "SK", "SI", "ES", "SE", "IS", "NO", "LI",
            "CH", "MC", "SM", "AD", "VA", "GB", "GI", "ME", "AL", "MK",
            "MD", "RS"
    );

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final ConcurrentMap<String, ReentrantLock> idempotencyLocks = new ConcurrentHashMap<>();

    public TransferService(
            AccountRepository accountRepository,
            TransferRepository transferRepository,
            TransactionRepository transactionRepository,
            ExchangeRateService exchangeRateService
    ) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.transactionRepository = transactionRepository;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional
    public TransferResponse createTransfer(CreateTransferRequest request) {
        String sourceIban = IbanUtils.normalize(request.sourceIban());
        String targetIban = IbanUtils.normalize(request.targetIban());
        String idempotencyKey = normalizeIdempotencyKey(request.idempotencyKey());
        BigDecimal amount = normalizeAmount(request.amount());

        if (idempotencyKey == null) {
            return createTransferInternal(sourceIban, targetIban, amount, null);
        }

        ReentrantLock lock = idempotencyLocks.computeIfAbsent(idempotencyKey, ignored -> new ReentrantLock());
        lock.lock();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    releaseIdempotencyLock(idempotencyKey, lock);
                }
            });
            return createTransferInternal(sourceIban, targetIban, amount, idempotencyKey);
        }

        try {
            return createTransferInternal(sourceIban, targetIban, amount, idempotencyKey);
        } finally {
            releaseIdempotencyLock(idempotencyKey, lock);
        }
    }

    private TransferResponse createTransferInternal(
            String sourceIban,
            String targetIban,
            BigDecimal amount,
            String idempotencyKey
    ) {
        if (idempotencyKey != null) {
            Transfer existingTransfer = transferRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
            if (existingTransfer != null) {
                return toResponse(existingTransfer);
            }
        }

        if (sourceIban.equals(targetIban)) {
            throw new SameAccountTransfer(sourceIban);
        }

        LockedAccounts lockedAccounts = lockAccounts(sourceIban, targetIban);
        Account sourceAccount = lockedAccounts.source();
        Account targetAccount = lockedAccounts.target();

        validateSepa(sourceAccount.getIban(), targetAccount.getIban());

        if (!isBankAccount(sourceAccount) && sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFunds(sourceAccount.getIban(), sourceAccount.getBalance(), amount);
        }

        validateSavingsDailyLimit(sourceAccount, amount);

        ExchangeRateService.ConversionResult conversion =
                exchangeRateService.convert(amount, sourceAccount.getCurrency(), targetAccount.getCurrency());
        BigDecimal targetAmount = conversion.targetAmount();

        if (!isBankAccount(sourceAccount)) {
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount).setScale(MONEY_SCALE, RoundingMode.HALF_EVEN));
        }
        if (!isBankAccount(targetAccount)) {
            targetAccount.setBalance(targetAccount.getBalance().add(targetAmount).setScale(MONEY_SCALE, RoundingMode.HALF_EVEN));
        }

        Transfer transfer = new Transfer(
                sourceAccount.getIban(),
                targetAccount.getIban(),
                amount,
                sourceAccount.getCurrency(),
                targetAccount.getCurrency(),
                conversion.exchangeRate(),
                conversion.convertedAmount(),
                idempotencyKey
        );

        Transfer savedTransfer = transferRepository.saveAndFlush(transfer);
        transactionRepository.saveAll(buildTransactions(savedTransfer, sourceAccount, targetAccount, amount, targetAmount));

        return toResponse(savedTransfer);
    }

    @Transactional(readOnly = true)
    public TransferResponse getTransferById(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFound(transferId));

        return toResponse(transfer);
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfers(String iban, Instant fromDate, Instant toDate, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be before or equal to toDate");
        }

        String normalizedIban = normalizeOptionalIban(iban);
        Specification<Transfer> specification = buildTransferSpecification(normalizedIban, fromDate, toDate);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").ascending().and(Sort.by("id").ascending()));

        return transferRepository.findAll(specification, pageRequest)
                .map(this::toResponse);
    }

    private LockedAccounts lockAccounts(String sourceIban, String targetIban) {
        String firstIban = sourceIban.compareTo(targetIban) <= 0 ? sourceIban : targetIban;
        String secondIban = sourceIban.compareTo(targetIban) <= 0 ? targetIban : sourceIban;

        Account firstAccount = accountRepository.findByIbanForUpdate(firstIban)
                .orElseThrow(() -> new AccountNotFound(firstIban));
        Account secondAccount = accountRepository.findByIbanForUpdate(secondIban)
                .orElseThrow(() -> new AccountNotFound(secondIban));

        Account sourceAccount = firstAccount.getIban().equals(sourceIban) ? firstAccount : secondAccount;
        Account targetAccount = firstAccount.getIban().equals(targetIban) ? firstAccount : secondAccount;

        return new LockedAccounts(sourceAccount, targetAccount);
    }

    private void validateSavingsDailyLimit(Account sourceAccount, BigDecimal amount) {
        if (isBankAccount(sourceAccount) || sourceAccount.getType() != AccountType.SAVINGS) {
            return;
        }

        Instant now = Instant.now();
        LocalDate todayUtc = now.atZone(ZoneOffset.UTC).toLocalDate();
        Instant dayStart = todayUtc.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant nextDayStart = todayUtc.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        BigDecimal todaysOutgoingTotal = ZERO;
        List<Transfer> todaysTransfers = transferRepository.findBySourceIbanAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                sourceAccount.getIban(),
                dayStart,
                nextDayStart
        );

        for (Transfer transfer : todaysTransfers) {
            todaysOutgoingTotal = todaysOutgoingTotal.add(toEurEquivalent(transfer.getAmount(), sourceAccount.getCurrency()));
        }

        BigDecimal currentTransferEurEquivalent = toEurEquivalent(amount, sourceAccount.getCurrency());
        BigDecimal newTotal = todaysOutgoingTotal.add(currentTransferEurEquivalent).setScale(MONEY_SCALE, RoundingMode.HALF_EVEN);
        if (newTotal.compareTo(SAVINGS_DAILY_LIMIT_EUR) > 0) {
            throw new SavingsDailyLimitExceeded(sourceAccount.getIban());
        }
    }

    private BigDecimal toEurEquivalent(BigDecimal amount, String sourceCurrency) {
        return exchangeRateService.convert(amount, sourceCurrency, "EUR").targetAmount();
    }

    private List<Transaction> buildTransactions(
            Transfer transfer,
            Account sourceAccount,
            Account targetAccount,
            BigDecimal sourceAmount,
            BigDecimal targetAmount
    ) {
        List<Transaction> transactions = new ArrayList<>();

        if (isBankAccount(sourceAccount)) {
            transactions.add(new Transaction(
                    targetAccount,
                    transfer,
                    transfer.getCreatedAt(),
                    TransactionType.DEPOSIT,
                    targetAmount,
                    targetAccount.getCurrency(),
                    targetAccount.getBalance(),
                    null
            ));
            return transactions;
        }

        if (isBankAccount(targetAccount)) {
            transactions.add(new Transaction(
                    sourceAccount,
                    transfer,
                    transfer.getCreatedAt(),
                    TransactionType.WITHDRAWAL,
                    sourceAmount,
                    sourceAccount.getCurrency(),
                    sourceAccount.getBalance(),
                    null
            ));
            return transactions;
        }

        transactions.add(new Transaction(
                sourceAccount,
                transfer,
                transfer.getCreatedAt(),
                TransactionType.TRANSFER_OUT,
                sourceAmount,
                sourceAccount.getCurrency(),
                sourceAccount.getBalance(),
                targetAccount.getIban()
        ));
        transactions.add(new Transaction(
                targetAccount,
                transfer,
                transfer.getCreatedAt(),
                TransactionType.TRANSFER_IN,
                targetAmount,
                targetAccount.getCurrency(),
                targetAccount.getBalance(),
                sourceAccount.getIban()
        ));

        return transactions;
    }

    private TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceIban(),
                transfer.getTargetIban(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getTargetCurrency(),
                transfer.getExchangeRate(),
                transfer.getConvertedAmount(),
                transfer.getIdempotencyKey(),
                transfer.getCreatedAt().toString()
        );
    }

    private void validateSepa(String sourceIban, String targetIban) {
        String sourceCountryCode = extractIbanCountryCode(sourceIban);
        String targetCountryCode = extractIbanCountryCode(targetIban);

        if (!SEPA_IBAN_COUNTRY_CODES.contains(sourceCountryCode) || !SEPA_IBAN_COUNTRY_CODES.contains(targetCountryCode)) {
            throw new SepaTransferNotAllowed(sourceIban, targetIban);
        }
    }

    private String extractIbanCountryCode(String iban) {
        if (iban == null || iban.length() < 2) {
            throw new SepaTransferNotAllowed(iban, iban);
        }

        return iban.substring(0, 2).toUpperCase(Locale.ROOT);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferAmount(amount);
        }

        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_EVEN);
    }

    private String normalizeOptionalIban(String iban) {
        if (iban == null || iban.isBlank()) {
            return null;
        }

        return IbanUtils.normalize(iban);
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }

        return idempotencyKey.trim();
    }

    private void releaseIdempotencyLock(String idempotencyKey, ReentrantLock lock) {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
        if (!lock.hasQueuedThreads()) {
            idempotencyLocks.remove(idempotencyKey, lock);
        }
    }

    private boolean isBankAccount(Account account) {
        return BANK_IBAN.equals(account.getIban());
    }

    private Specification<Transfer> buildTransferSpecification(String iban, Instant fromDate, Instant toDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (iban != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("sourceIban"), iban),
                        criteriaBuilder.equal(root.get("targetIban"), iban)
                ));
            }
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private record LockedAccounts(Account source, Account target) {
    }
}

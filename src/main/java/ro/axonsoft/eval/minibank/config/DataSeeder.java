package ro.axonsoft.eval.minibank.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.repository.AccountRepository;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    private static final String BANK_IBAN = "RO49AAAA1B31007593840000";

    @Bean
    CommandLineRunner seedBankAccount(AccountRepository accountRepository) {
        return args -> {
            if (accountRepository.findByIban(BANK_IBAN).isEmpty()) {
                Account bankAccount = new Account(
                        "MiniBank System Account",
                        BANK_IBAN,
                        "RON",
                        AccountType.CHECKING
                );
                bankAccount.setBalance(new BigDecimal("1000000000.00"));

                accountRepository.save(bankAccount);
            }
        };
    }
}
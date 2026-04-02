package ro.axonsoft.eval.minibank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.axonsoft.eval.minibank.model.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIban(String iban);
    boolean existsByIban(String iban);
}

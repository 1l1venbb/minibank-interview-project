package ro.axonsoft.eval.minibank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ro.axonsoft.eval.minibank.model.Transfer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long>, JpaSpecificationExecutor<Transfer> {
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<Transfer> findBySourceIbanAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String sourceIban,
            Instant startInclusive,
            Instant endExclusive
    );
}

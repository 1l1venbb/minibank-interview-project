package ro.axonsoft.eval.minibank.dto;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String ownerName,
        String iban,
        String currency,
        String accountType,
        BigDecimal balance,
        String createdAt
) {
}

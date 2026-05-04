package ro.axonsoft.eval.minibank.dto;

import java.math.BigDecimal;

public record TransferResponse(
        Long id,
        String sourceIban,
        String targetIban,
        BigDecimal amount,
        String currency,
        String targetCurrency,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount,
        String idempotencyKey,
        String createdAt
) {
}

package ro.axonsoft.eval.minibank.dto;

import java.math.BigDecimal;

public record TransactionResponse(
        Long id,
        String timestamp,
        String type,
        BigDecimal amount,
        String currency,
        BigDecimal balanceAfter,
        String counterpartyIban,
        Long transferId
) {
}

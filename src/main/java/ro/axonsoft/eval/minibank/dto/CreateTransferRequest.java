package ro.axonsoft.eval.minibank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTransferRequest(
        @NotBlank String sourceIban,
        @NotBlank String targetIban,
        @NotNull @Positive BigDecimal amount,
        String idempotencyKey
) {
}

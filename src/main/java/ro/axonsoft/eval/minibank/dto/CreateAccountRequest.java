package ro.axonsoft.eval.minibank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank String ownerName, @NotBlank String iban,
        @NotBlank @Size(min = 3, max = 3) String currency, @NotBlank String type
) {
}

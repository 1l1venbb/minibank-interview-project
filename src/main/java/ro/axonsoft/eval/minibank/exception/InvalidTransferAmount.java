package ro.axonsoft.eval.minibank.exception;

import java.math.BigDecimal;

public class InvalidTransferAmount extends RuntimeException {
    public InvalidTransferAmount(BigDecimal amount) {
        super("Transfer amount must be positive: " + amount);
    }
}

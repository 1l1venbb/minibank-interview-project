package ro.axonsoft.eval.minibank.exception;

import java.math.BigDecimal;

public class InsufficientFunds extends RuntimeException {
    public InsufficientFunds(String iban, BigDecimal balance, BigDecimal amount) {
        super("Insufficient funds for account " + iban + ": balance=" + balance + ", amount=" + amount);
    }
}

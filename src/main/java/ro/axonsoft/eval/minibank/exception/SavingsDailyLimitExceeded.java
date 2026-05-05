package ro.axonsoft.eval.minibank.exception;

public class SavingsDailyLimitExceeded extends RuntimeException {
    public SavingsDailyLimitExceeded(String iban) {
        super("Daily outgoing transfer limit exceeded for savings account: " + iban);
    }
}

package ro.axonsoft.eval.minibank.exception;

public class InvalidIban extends RuntimeException {
    public InvalidIban(String iban) {
        super("Invalid IBAN: " + iban);
    }
}

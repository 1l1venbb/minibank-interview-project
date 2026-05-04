package ro.axonsoft.eval.minibank.exception;

public class AccountNotFound extends RuntimeException {
    public AccountNotFound(Long id) {
        super("Account not found with id: " + id);
    }

    public AccountNotFound(String iban) {
        super("Account not found with iban: " + iban);
    }
}

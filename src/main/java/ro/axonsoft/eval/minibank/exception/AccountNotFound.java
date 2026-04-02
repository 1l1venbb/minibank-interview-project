package ro.axonsoft.eval.minibank.exception;

public class AccountNotFound extends RuntimeException {
    public AccountNotFound(Long id) {
        super("Account not found with id: " + id);
    }
}

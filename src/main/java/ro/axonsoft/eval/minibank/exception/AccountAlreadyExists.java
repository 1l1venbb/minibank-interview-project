package ro.axonsoft.eval.minibank.exception;

public class AccountAlreadyExists extends RuntimeException{
    public AccountAlreadyExists(String iban) {
        super("Account already exists with iban: " + iban);
    }
}

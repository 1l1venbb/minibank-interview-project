package ro.axonsoft.eval.minibank.exception;

public class InvalidAccountType extends RuntimeException {
    public InvalidAccountType(String type) {
        super("Invalid account type: " + type);
    }
}

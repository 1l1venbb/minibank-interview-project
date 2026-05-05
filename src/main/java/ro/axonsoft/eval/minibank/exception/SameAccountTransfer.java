package ro.axonsoft.eval.minibank.exception;

public class SameAccountTransfer extends RuntimeException {
    public SameAccountTransfer(String iban) {
        super("Source and target account must be different: " + iban);
    }
}

package ro.axonsoft.eval.minibank.exception;

public class SepaTransferNotAllowed extends RuntimeException {
    public SepaTransferNotAllowed(String sourceIban, String targetIban) {
        super("Transfer not allowed between non-SEPA accounts: " + sourceIban + " -> " + targetIban);
    }
}

package ro.axonsoft.eval.minibank.exception;

public class UnsupportedCurrency extends RuntimeException {
    public UnsupportedCurrency(String currency) {
        super("Unsupported currency: " + currency);
    }
}

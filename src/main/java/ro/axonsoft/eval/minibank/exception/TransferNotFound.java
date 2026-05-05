package ro.axonsoft.eval.minibank.exception;

public class TransferNotFound extends RuntimeException {
    public TransferNotFound(Long transferId) {
        super("Transfer not found with id: " + transferId);
    }
}

package ro.axonsoft.eval.minibank.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ro.axonsoft.eval.minibank.dto.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(AccountAlreadyExists.class)
    public ResponseEntity<ApiError> handleAccountAlreadyExists(AccountAlreadyExists e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.rejected(e.getMessage()));
    }

    @ExceptionHandler(InvalidAccountType.class)
    public ResponseEntity<ApiError> handleInvalidAccountType(InvalidAccountType e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.rejected(e.getMessage()));
    }

    @ExceptionHandler(UnsupportedCurrency.class)
    public ResponseEntity<ApiError> handleUnsupportedCurrency(UnsupportedCurrency e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.rejected(e.getMessage()));
    }

    @ExceptionHandler(AccountNotFound.class)
    public ResponseEntity<ApiError> handleAccountNotFound(AccountNotFound e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.rejected(e.getMessage()));
    }

    @ExceptionHandler(TransferNotFound.class)
    public ResponseEntity<ApiError> handleTransferNotFound(TransferNotFound e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.rejected(e.getMessage()));
    }

    @ExceptionHandler(InvalidTransferAmount.class)
    public ResponseEntity<ApiError> handleInvalidTransferAmount(InvalidTransferAmount e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.rejected(e.getMessage()));
    }

    @ExceptionHandler(InvalidIban.class)
    public ResponseEntity<ApiError> handleInvalidIban(InvalidIban e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.rejected(e.getMessage()));
    }

    @ExceptionHandler({
            InsufficientFunds.class,
            SameAccountTransfer.class,
            SavingsDailyLimitExceeded.class,
            SepaTransferNotAllowed.class
    })
    public ResponseEntity<ApiError> handleTransferConflict(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.rejected(e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.rejected("Resource already exists"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.rejected(message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String parameterName = e.getName() == null ? "parameter" : e.getName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.rejected("Invalid value for parameter: " + parameterName));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableMessage(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.rejected("Malformed request body"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.rejected(e.getMessage()));
    }
}

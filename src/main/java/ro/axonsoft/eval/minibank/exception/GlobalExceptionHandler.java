package ro.axonsoft.eval.minibank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ro.axonsoft.eval.minibank.dto.ApiError;
import org.springframework.dao.DataIntegrityViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(AccountAlreadyExists.class)
    public ResponseEntity<ApiError> handleAccountAlreadyExists(AccountAlreadyExists e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("ACCOUNT_ALREADY_EXISTS", e.getMessage()));
    }

    @ExceptionHandler(InvalidAccountType.class)
    public ResponseEntity<ApiError> handleInvalidAccountType(InvalidAccountType e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("INVALID_ACCOUNT_TYPE", e.getMessage()));
    }

    @ExceptionHandler(UnsupportedCurrency.class)
    public ResponseEntity<ApiError> handleUnsupportedCurrency(UnsupportedCurrency e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("UNSUPPORTED_CURRENCY", e.getMessage()));
    }

    @ExceptionHandler(AccountNotFound.class)
    public ResponseEntity<ApiError> handleAccountNotFound(AccountNotFound e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("ACCOUNT_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("ACCOUNT_ALREADY_EXISTS", "Account already exists with this iban"));
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
                .body(new ApiError("VALIDATION_ERROR", message));
    }
}

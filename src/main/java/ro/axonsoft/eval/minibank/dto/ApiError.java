package ro.axonsoft.eval.minibank.dto;

public record ApiError(
    String status,
    String message
){
    public static ApiError rejected(String message) {
        return new ApiError("REJECTED", message);
    }
}

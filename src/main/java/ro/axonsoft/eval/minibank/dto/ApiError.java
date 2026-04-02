package ro.axonsoft.eval.minibank.dto;

public record ApiError(
    String code,
    String message
){
}

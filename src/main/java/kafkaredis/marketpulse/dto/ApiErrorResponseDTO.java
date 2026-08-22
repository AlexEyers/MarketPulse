package kafkaredis.marketpulse.dto;

public record ApiErrorResponseDTO(
        String error,
        String message
) {
}

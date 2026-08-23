package kafkaredis.marketpulse.dto;

public record TwelveDataPriceResponseDTO (
        String price,
        String status,
        String message,
        Integer code
) {
}

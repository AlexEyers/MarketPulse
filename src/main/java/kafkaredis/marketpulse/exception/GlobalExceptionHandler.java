package kafkaredis.marketpulse.exception;

import kafkaredis.marketpulse.dto.ApiErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 Bad Request
    @ExceptionHandler(InvalidSymbolException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidSymbolException(InvalidSymbolException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponseDTO(
                "INVALID_SYMBOL",
                ex.getMessage()
        ));
    }

    // 502 Bad Gateway
    @ExceptionHandler(MarketDataUnavailableException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleMarketDataUnavailableException(MarketDataUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiErrorResponseDTO(
                "MARKET_DATA_UNAVAILABLE",
                ex.getMessage()
        ));
    }

    // 404 Not Found
    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleStockNotFoundException(StockNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponseDTO(
                "STOCK_NOT_FOUND",
                ex.getMessage()
        ));
    }
}

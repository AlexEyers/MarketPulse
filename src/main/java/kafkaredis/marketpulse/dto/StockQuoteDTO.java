package kafkaredis.marketpulse.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record StockQuoteDTO (
        String symbol,
        BigDecimal price,
        Instant timestamp
    ) {
    }


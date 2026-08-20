package kafkaredis.marketpulse.client;

import kafkaredis.marketpulse.dto.StockQuoteDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class MarketDataClient {

    public StockQuoteDTO getStock(String symbol) {
        return new StockQuoteDTO(symbol, new BigDecimal("145.67"), Instant.now());
    }
}

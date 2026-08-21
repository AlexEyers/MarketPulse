package kafkaredis.marketpulse.client;

import kafkaredis.marketpulse.config.TwelveDataProperties;
import kafkaredis.marketpulse.dto.StockQuoteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TwelveDataClient implements MarketDataClient{

    private final TwelveDataProperties twelveDataProperties;

    @Override
    public StockQuoteDTO getStock(String symbol) {
        return new StockQuoteDTO(symbol, new BigDecimal("145.67"), Instant.now());
    }
}

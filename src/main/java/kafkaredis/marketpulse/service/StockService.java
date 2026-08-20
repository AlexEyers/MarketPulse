package kafkaredis.marketpulse.service;

import kafkaredis.marketpulse.client.MarketDataClient;
import kafkaredis.marketpulse.dto.StockQuoteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

    private final MarketDataClient marketDataClient;

    public StockQuoteDTO getStock(String symbol) {
        return marketDataClient.getStock(symbol.toUpperCase());
    }
}

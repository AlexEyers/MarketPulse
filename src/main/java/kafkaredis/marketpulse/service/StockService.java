package kafkaredis.marketpulse.service;

import kafkaredis.marketpulse.client.MarketDataClient;
import kafkaredis.marketpulse.dto.StockQuoteDTO;
import kafkaredis.marketpulse.exception.InvalidSymbolException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

    private static final String SYMBOL_PATTERN = "[A-Z0-9.-]{1,15}";

    private final MarketDataClient marketDataClient;

    public StockQuoteDTO getStock(String symbol) {

        String normalizedSymbol = symbol.trim().toUpperCase();

        if(normalizedSymbol.isBlank()) {
            throw new InvalidSymbolException("Stock symbol must not be blank");
        }

        if(!normalizedSymbol.matches(SYMBOL_PATTERN)) {
            throw new InvalidSymbolException("Stock symbol format is invalid");
        }

        return marketDataClient.getStock(normalizedSymbol);
    }
}

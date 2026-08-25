package kafkaredis.marketpulse.service;

import kafkaredis.marketpulse.client.MarketDataClient;
import kafkaredis.marketpulse.dto.StockQuoteDTO;
import kafkaredis.marketpulse.exception.InvalidSymbolException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService {

    private static final String SYMBOL_PATTERN = "[A-Z0-9.-]{1,15}";

    private final MarketDataClient marketDataClient;
    private final StockCacheService stockCacheService;

    public StockQuoteDTO getStock(String symbol) {

        String normalizedSymbol = symbol.trim().toUpperCase();

        if(normalizedSymbol.isBlank()) {
            throw new InvalidSymbolException("Stock symbol must not be blank");
        }

        if(!normalizedSymbol.matches(SYMBOL_PATTERN)) {
            throw new InvalidSymbolException("Stock symbol format is invalid");
        }

        // Optional.empty() if not in cache or Optional containing StockQuoteDTO if it is already in cache
        Optional<StockQuoteDTO> cachedQuote = stockCacheService.get(normalizedSymbol);

        if(cachedQuote.isPresent()) { // If the Optional contains a value, then Redis has the quote cached
            return cachedQuote.get(); // Return as StockQuoteDTO not Optional<StockQuoteDTO>
        }
        // If not in cache, call API and then store the response in cache
        StockQuoteDTO quote = marketDataClient.getStock(normalizedSymbol);
        stockCacheService.put(normalizedSymbol, quote);

        return quote; // Return API response
    }
}

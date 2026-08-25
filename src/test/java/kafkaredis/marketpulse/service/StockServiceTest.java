package kafkaredis.marketpulse.service;

import kafkaredis.marketpulse.client.MarketDataClient;
import kafkaredis.marketpulse.dto.StockQuoteDTO;
import kafkaredis.marketpulse.exception.InvalidSymbolException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class StockServiceTest {

    // Test to check that StockService passes a trimmed and uppercased symbol to the market data client
    @Test
    void getStock_trimsAndUpperCasesSymbol() {
        // MarketDataClient = functional interface (interface with 1 abstract method)
        // "symbol" is the argument that will be passed into getStock() later
        // When StockService calls fakeClient.getStock("AAPL"), this returns a fake quote using "AAPL"
        // Lambda : Left of -> is method input : symbol
        // Lambda : Right of -> is method return value : new StockQuoteDTO(...)
        MarketDataClient fakeClient = symbol -> new StockQuoteDTO(
                symbol,
                new BigDecimal("123.45"),
                Instant.now()
        ); // Alternatively : = new MarketDataClient() {getStock() {return new StockQuoteDTO(....) {}}}

        StockCacheService stockCacheService = mock(StockCacheService.class); // Fake StockCacheService
        when(stockCacheService.get(anyString())).thenReturn(Optional.empty()); // Return Optional.empty() - simulate a cache miss

        StockService stockService = new StockService(fakeClient, stockCacheService);

        StockQuoteDTO result = stockService.getStock(" aapl ");

        assertEquals("AAPL", result.symbol());
    }

    // Test to check that app throws an InvalidSymbolException for a blank symbol input
    @Test
    void getStock_rejectsBlankSymbol() {
        MarketDataClient fakeClient = symbol -> fail("Market data client should not be called");

        StockCacheService stockCacheService = mock(StockCacheService.class);

        StockService stockService = new StockService(fakeClient, stockCacheService);

        assertThrows(InvalidSymbolException.class, () -> stockService.getStock("  "));
    }

    // Test to check that app throws an InvalidSymbolException for an invalid symbol input
    @Test
    void getStock_rejectsInvalidSymbolFormat() {

        MarketDataClient fakeClient = symbol -> fail("Market data client should not be called");

        StockCacheService stockCacheService = mock(StockCacheService.class);

        StockService stockService = new StockService(fakeClient, stockCacheService);

        assertThrows(InvalidSymbolException.class, () -> stockService.getStock("%$#"));
    }

    @Test
    void getStock_returnsCachedQuoteWhenCacheHit() {
        StockQuoteDTO cachedQuote = new StockQuoteDTO(
                "AAPL",
                new BigDecimal("123.45"),
                Instant.now()
        );

        MarketDataClient fakeClient = symbol -> fail("Market data client should not be called");

        StockCacheService stockCacheService = mock(StockCacheService.class);
        when(stockCacheService.get("AAPL")).thenReturn(Optional.of(cachedQuote));

        StockService stockService = new StockService(fakeClient, stockCacheService);

        StockQuoteDTO result = stockService.getStock("AAPL");

        assertEquals("AAPL", result.symbol());
        verify(stockCacheService).get("AAPL"); // Verify this mocked method was called (Redis was checked for AAPL)
        verify(stockCacheService, never()).put("AAPL", cachedQuote); // Check StockService did not save anything to Redis as it was already cached
    }

    @Test
    void getStock_storesQuoteInCacheWhenCacheMiss() {

        StockQuoteDTO apiQuote = new StockQuoteDTO(
                "AAPL",
                new BigDecimal("123.45"),
                Instant.now()
        ); // Simulate external api response

        // If StockService calls fakeClient.getStock("AAPL"), return apiQuote
        MarketDataClient fakeClient = symbol -> apiQuote;

        // Mock Redis, and simulate a cache miss when redis .get() is called
        StockCacheService stockCacheService = mock(StockCacheService.class);
        when(stockCacheService.get("AAPL")).thenReturn(Optional.empty());

        StockService stockService = new StockService(fakeClient, stockCacheService);

        StockQuoteDTO result = stockService.getStock("AAPL");
        assertEquals(apiQuote,result);  // Check that the result is our simulated api response
        verify(stockCacheService).get("AAPL"); // Check StockService looked in the cache for AAPL first
        verify(stockCacheService).put("AAPL", apiQuote); // Verify StockService saved the API result into the cache after the cache miss
    }
}

package kafkaredis.marketpulse.service;

import kafkaredis.marketpulse.client.MarketDataClient;
import kafkaredis.marketpulse.dto.StockQuoteDTO;
import kafkaredis.marketpulse.exception.InvalidSymbolException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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

        StockService stockService = new StockService(fakeClient);

        StockQuoteDTO result = stockService.getStock(" aapl ");

        assertEquals("AAPL", result.symbol());
    }

    // Test to check that app throws an InvalidSymbolException for a blank symbol input
    @Test
    void getStock_rejectsBlankSymbol() {
        MarketDataClient fakeClient = symbol -> fail("Market data client should not be called");

        StockService stockService = new StockService(fakeClient);
        assertThrows(InvalidSymbolException.class, () -> stockService.getStock("  "));
    }

    // Test to check that app throws an InvalidSymbolException for an invalid symbol input
    @Test
    void getStock_rejectsInvalidSymbolFormat() {

        MarketDataClient fakeClient = symbol -> fail("Market data client should not be called");

        StockService stockService = new StockService(fakeClient);
        assertThrows(InvalidSymbolException.class, () -> stockService.getStock("%$#"));
    }
}

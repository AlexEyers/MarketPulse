package kafkaredis.marketpulse.controller;

import kafkaredis.marketpulse.client.MarketDataClient;
import kafkaredis.marketpulse.dto.StockQuoteDTO;
import kafkaredis.marketpulse.exception.GlobalExceptionHandler;
import kafkaredis.marketpulse.exception.MarketDataUnavailableException;
import kafkaredis.marketpulse.exception.StockNotFoundException;
import kafkaredis.marketpulse.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import kafkaredis.marketpulse.service.StockCacheService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StockControllerTest {

    // Test to see if a valid request returns a 200 OK with a normalized symbol and price
    @Test
    void getStock_returnsQuoteForValidSymbol() throws Exception { // mockMvc.perform(get("x")) is a checked exception

        // fakeClient.getStock(symbol) returns  fake StockQuoteDTO instead of calling real Twelve Data API
        MarketDataClient fakeClient = symbol -> new StockQuoteDTO(
                symbol,
                new BigDecimal("123.45"),
                Instant.now()
        );

        // Build Controller -> StockService -> StockCacheService -> Fake MarketDataClient chain
        StockCacheService stockCacheService = mock(StockCacheService.class);
        when(stockCacheService.get(anyString())).thenReturn(Optional.empty());
        StockService stockService = new StockService(fakeClient, stockCacheService);

        StockController stockController = new StockController(stockService);

        // Create in-memory MVC test setup
        // setControllerAdvice includes GlobalExceptionHandler so error responses are handled like the real app
        MockMvc mockMvc = standaloneSetup(stockController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Mock the GET request and check if returned values are what we expected.
        mockMvc.perform(get("/api/stocks/aapl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.price").value(123.45));
    }

    // Test to see if an invalid symbol format returns a 400 Bad Request with an INVALID_SYMBOL error body
    @Test
    void getStock_returns400ForInvalidSymbolFormat() throws Exception {

        // Throw exception if fakeClient is called, as invalid symbols should be rejected in the Service layer
        MarketDataClient fakeClient = symbol -> {
            throw new AssertionError("Market data client should not be called");
        };

        // no when(...) needed as should never even get to the check cache point
        StockCacheService stockCacheService = mock(StockCacheService.class);
        StockService stockService = new StockService(fakeClient, stockCacheService);

        StockController stockController = new StockController(stockService);

        MockMvc mockMvc = standaloneSetup(stockController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/stocks/AAPL!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_SYMBOL"))
                .andExpect(jsonPath("$.message").value("Stock symbol format is invalid"));
    }

    // Test to see if StockNotFoundException is converted into 404 Not Found with a STOCK_NOT_FOUND error body
    @Test
    void getStock_returns404WhenStockIsNotFound() throws Exception {

        MarketDataClient fakeClient = symbol -> {
            throw new StockNotFoundException("Stock symbol was not found");
        };

        StockCacheService stockCacheService = mock(StockCacheService.class);
        when(stockCacheService.get(anyString())).thenReturn(Optional.empty());
        StockService stockService = new StockService(fakeClient, stockCacheService);

        StockController stockController = new StockController(stockService);

        MockMvc mockMvc = standaloneSetup(stockController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/stocks/ZZZZZZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("STOCK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Stock symbol was not found"));
    }

    // Test to see if MarketDataUnavailableException is converted into 502 Bad Gateway with a MARKET_DATA_UNAVAILABLE error body
    @Test
    void getStock_returns502WhenMarketDataIsUnavailable() throws Exception {
        MarketDataClient fakeClient = symbol -> {
            throw new MarketDataUnavailableException("Market data provider is unavailable");
        };

        StockCacheService stockCacheService = mock(StockCacheService.class);
        when(stockCacheService.get(anyString())).thenReturn(Optional.empty());
        StockService stockService = new StockService(fakeClient, stockCacheService);

        StockController stockController = new StockController(stockService);

        MockMvc mockMvc = standaloneSetup(stockController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/stocks/AAPL"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("MARKET_DATA_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Market data provider is unavailable"));
    }
}

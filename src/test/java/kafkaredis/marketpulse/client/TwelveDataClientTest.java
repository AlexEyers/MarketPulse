package kafkaredis.marketpulse.client;

import kafkaredis.marketpulse.config.TwelveDataProperties;
import kafkaredis.marketpulse.dto.StockQuoteDTO;
import kafkaredis.marketpulse.exception.MarketDataUnavailableException;
import kafkaredis.marketpulse.exception.StockNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TwelveDataClientTest {

    private static final String BASE_URL = "https://api.twelvedata.com";
    private static final String API_KEY = "test-api-key";

    private MockRestServiceServer server;
    private TwelveDataClient twelveDataClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        // Mock server attaches to this RestClient builder and intercepts HTTP requests in memory.
        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder
                .baseUrl(BASE_URL)
                .build();

        TwelveDataProperties properties = new TwelveDataProperties(BASE_URL, API_KEY);

        twelveDataClient = new TwelveDataClient(restClient, properties);
    }

    @Test
    void getStock_returnsQuoteWhenProviderReturnsPrice() {
        server.expect(once(), requestTo(BASE_URL + "/price?symbol=AAPL&apikey=" + API_KEY))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "price": "123.45"
                        }
                        """, MediaType.APPLICATION_JSON));

        StockQuoteDTO result = twelveDataClient.getStock("AAPL");

        assertEquals("AAPL", result.symbol());
        assertEquals(new BigDecimal("123.45"), result.price());

        server.verify();
    }

    @Test
    void getStock_throwsStockNotFoundWhenProviderReturnsErrorCode400() {
        server.expect(once(), requestTo(BASE_URL + "/price?symbol=ZZZZZZ&apikey=" + API_KEY))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "code": 400,
                          "message": "Invalid parameter: symbol",
                          "status": "error"
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThrows(StockNotFoundException.class, () -> twelveDataClient.getStock("ZZZZZZ"));

        server.verify();
    }

    @Test
    void getStock_throwsMarketDataUnavailableWhenProviderReturnsOtherErrorCode() {
        server.expect(once(), requestTo(BASE_URL + "/price?symbol=AAPL&apikey=" + API_KEY))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "code": 429,
                          "message": "API rate limit exceeded",
                          "status": "error"
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThrows(MarketDataUnavailableException.class, () -> twelveDataClient.getStock("AAPL"));

        server.verify();
    }

    @Test
    void getStock_throwsMarketDataUnavailableWhenPriceIsMissing() {
        server.expect(once(), requestTo(BASE_URL + "/price?symbol=AAPL&apikey=" + API_KEY))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThrows(MarketDataUnavailableException.class, () -> twelveDataClient.getStock("AAPL"));

        server.verify();
    }

    @Test
    void getStock_throwsMarketDataUnavailableWhenPriceIsMalformed() {
        server.expect(once(), requestTo(BASE_URL + "/price?symbol=AAPL&apikey=" + API_KEY))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "price": "N/A"
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThrows(MarketDataUnavailableException.class, () -> twelveDataClient.getStock("AAPL"));

        server.verify();
    }

    @Test
    void getStock_throwsMarketDataUnavailableWhenProviderReturnsHttpError() {
        server.expect(once(), requestTo(BASE_URL + "/price?symbol=AAPL&apikey=" + API_KEY))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(MarketDataUnavailableException.class, () -> twelveDataClient.getStock("AAPL"));

        server.verify();
    }
}

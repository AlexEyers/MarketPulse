package kafkaredis.marketpulse.client;

import kafkaredis.marketpulse.config.TwelveDataProperties;
import kafkaredis.marketpulse.dto.StockQuoteDTO;
import kafkaredis.marketpulse.dto.TwelveDataPriceResponseDTO;
import kafkaredis.marketpulse.exception.MarketDataUnavailableException;
import kafkaredis.marketpulse.exception.StockNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TwelveDataClient implements MarketDataClient {

    private final TwelveDataProperties twelveDataProperties; // baseUrl and apiKey

    @Override
    public StockQuoteDTO getStock(String symbol) {
        RestClient restClient = RestClient.create(twelveDataProperties.baseUrl());
        TwelveDataPriceResponseDTO response;

        // Call Twelve Data API, if the request fails before getting a usable response, throw exception.
        try {
            response = restClient.get().uri(
                            uriBuilder -> uriBuilder
                                    .path("/price")
                                    .queryParam("symbol", symbol)
                                    .queryParam("apikey", twelveDataProperties.apiKey())
                                    .build())
                    .retrieve()
                    .body(TwelveDataPriceResponseDTO.class);
        } catch (RestClientException ex) { // Catch provider/network/API failures.
            throw new MarketDataUnavailableException("Market data provider is unavailable");
        }

        // Catch no response body, and throw an exception
        if (response == null) {
            throw new MarketDataUnavailableException("Market data provider returned an empty response");
        }

        // Catch "error" response from Twelve Data API.
        if ("error".equalsIgnoreCase(response.status())) {
            if (response.code() != null && response.code() == 400) {
                throw new StockNotFoundException("Stock symbol was not found");
            }
            throw new MarketDataUnavailableException("Market data provider returned an error");
        }

        // Catch no price or blank price errors
        if (response.price() == null || response.price().isBlank()) {
            throw new MarketDataUnavailableException("Market data provider did not return a price");
        }

        BigDecimal price;

        try { // Check if returned price can be converted safely to a BigDecimal, catch error if it cannot.
            price = new BigDecimal(response.price());
        } catch (NumberFormatException ex) {
            throw new MarketDataUnavailableException("Market data provider returned an invalid price");
        }

        return new StockQuoteDTO(
                symbol,
                price,
                Instant.now()
        );
    }
}

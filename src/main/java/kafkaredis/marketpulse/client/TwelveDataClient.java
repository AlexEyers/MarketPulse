package kafkaredis.marketpulse.client;

import kafkaredis.marketpulse.config.TwelveDataProperties;
import kafkaredis.marketpulse.dto.StockQuoteDTO;
import kafkaredis.marketpulse.dto.TwelveDataPriceResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TwelveDataClient implements MarketDataClient{

    private final TwelveDataProperties twelveDataProperties; // baseUrl and apiKey

    @Override
    public StockQuoteDTO getStock(String symbol) {
        RestClient restClient = RestClient.create(twelveDataProperties.baseUrl());
        TwelveDataPriceResponseDTO response = restClient.get().uri(
                        uriBuilder -> uriBuilder
                                .path("/price")
                                .queryParam("symbol", symbol)
                                .queryParam("apikey", twelveDataProperties.apiKey())
                                .build())
                .retrieve()
                .body(TwelveDataPriceResponseDTO.class);

        return new StockQuoteDTO(
                symbol,
                new BigDecimal(response.price()),
                Instant.now()
        );
    }
}

package kafkaredis.marketpulse.client;

import kafkaredis.marketpulse.dto.StockQuoteDTO;

public interface MarketDataClient {

    StockQuoteDTO getStock(String symbol);
}

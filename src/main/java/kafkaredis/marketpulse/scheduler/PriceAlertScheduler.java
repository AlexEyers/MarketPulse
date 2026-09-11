package kafkaredis.marketpulse.scheduler;

import kafkaredis.marketpulse.dto.StockQuoteDTO;
import kafkaredis.marketpulse.entity.PriceAlert;
import kafkaredis.marketpulse.entity.PriceAlertStatus;
import kafkaredis.marketpulse.repository.PriceAlertRepository;
import kafkaredis.marketpulse.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PriceAlertScheduler {

    private final PriceAlertRepository priceAlertRepository;
    private final StockService stockService;

    // Use configured delay value if present, else default to 900000ms (15minutes)
    @Scheduled(fixedDelayString = "${market-pulse.alerts.scheduler.fixed-delay-ms:900000}")
    @Transactional
    public void checkPriceAlerts() {
        Instant now = Instant.now();

        expireOlderAlerts(now);

        // Get all distinct active symbols
        List<String> symbols = priceAlertRepository.findDistinctActiveSymbols(
                PriceAlertStatus.ACTIVE,
                now
        );

        // For every distinct active symbol, call external API to fetch it's current price.
        // Then, get all active, non-expired PriceAlerts, and for each alert :
        // Check if the alert should be triggered, and if so, mark it as triggered
        for (String symbol : symbols) {
            StockQuoteDTO quote = stockService.getStock(symbol);

            List<PriceAlert> alerts = priceAlertRepository.findByStatusAndSymbolAndExpiresAtAfter(
                    PriceAlertStatus.ACTIVE,
                    symbol,
                    now
            );

            for (PriceAlert alert : alerts) {
                if (alert.isTriggeredBy(quote.price())) {
                    alert.markTriggered();
                }
            }
        }
    }
        // Set expired alerts to PriceAlertStatus.EXPIRED
        private void expireOlderAlerts(Instant now) {

        // Get all PriceAlert's which have expiresAt < current time
        List<PriceAlert> expiredAlerts = priceAlertRepository.findByStatusAndExpiresAtLessThanEqual(
                PriceAlertStatus.ACTIVE,
                now
        );

        for(PriceAlert alert : expiredAlerts) {
            alert.markExpired();
        }
    }
}

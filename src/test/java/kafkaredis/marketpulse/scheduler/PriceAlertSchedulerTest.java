package kafkaredis.marketpulse.scheduler;

import kafkaredis.marketpulse.dto.StockQuoteDTO;
import kafkaredis.marketpulse.entity.PriceAlert;
import kafkaredis.marketpulse.entity.PriceAlertConditionType;
import kafkaredis.marketpulse.entity.PriceAlertStatus;
import kafkaredis.marketpulse.entity.User;
import kafkaredis.marketpulse.repository.PriceAlertRepository;
import kafkaredis.marketpulse.service.StockService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PriceAlertSchedulerTest {

    private final PriceAlertRepository priceAlertRepository = mock(PriceAlertRepository.class);
    private final StockService stockService = mock(StockService.class);

    private final PriceAlertScheduler priceAlertScheduler =
            new PriceAlertScheduler(priceAlertRepository, stockService);

    // Test that 0 active symbols means the StockService is not called
    @Test
    void checkPriceAlerts_doesNotCallStockServiceWhenNoActiveSymbolsExist() {
        // When scheduler checks if there are any expired ACTIVE alerts,
        // return an empty list
        when(priceAlertRepository.findByStatusAndExpiresAtLessThanEqual(
                eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of());
        // When scheduler checks which active symbols need price checking, return an empty list
        when(priceAlertRepository.findDistinctActiveSymbols(eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of());

        priceAlertScheduler.checkPriceAlerts();
        // Check StockService.getStock() was not called at all
        verify(stockService, never()).getStock(any());
    }

    // Test the scheduler fetches each distinct active symbol once
    @Test
    void checkPriceAlerts_fetchesEachDistinctSymbolOnce() {
        when(priceAlertRepository.findByStatusAndExpiresAtLessThanEqual(
                eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of());

        when(priceAlertRepository.findDistinctActiveSymbols(eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of("AAPL", "MSFT"));

        when(stockService.getStock("AAPL")).thenReturn(
                new StockQuoteDTO("AAPL", new BigDecimal("210.0000"), Instant.now()));

        when(stockService.getStock("MSFT")).thenReturn(
                new StockQuoteDTO("MSFT", new BigDecimal("300.0000"), Instant.now()));

        when(priceAlertRepository.findByStatusAndSymbolAndExpiresAtAfter(
                eq(PriceAlertStatus.ACTIVE), any(), any()))
                        .thenReturn(List.of());

        priceAlertScheduler.checkPriceAlerts();

        verify(stockService).getStock("AAPL");
        verify(stockService).getStock("MSFT");
    }

    // Test that an ABOVE alert is marked TRIGGERED when the current price is above the target price
    @Test
    void checkPriceAlerts_marksAboveAlertTriggeredWhenCurrentPriceIsAboveTarget() {
        User user = new User("user", "user@example.com", "hashed-password");
        PriceAlert alert = new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        );

        when(priceAlertRepository.findByStatusAndExpiresAtLessThanEqual(
                eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of());

        when(priceAlertRepository.findDistinctActiveSymbols(eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of("AAPL"));

        when(stockService.getStock("AAPL")).thenReturn(
                new StockQuoteDTO("AAPL", new BigDecimal("210.0000"), Instant.now()));

        when(priceAlertRepository.findByStatusAndSymbolAndExpiresAtAfter(
                eq(PriceAlertStatus.ACTIVE), eq("AAPL"), any()))
                .thenReturn(List.of(alert));

        priceAlertScheduler.checkPriceAlerts();

        assertEquals(PriceAlertStatus.TRIGGERED, alert.getStatus());
    }

    // Test that a BELOW alert is marked TRIGGERED when the current price is below the target price
    @Test
    void checkPriceAlerts_marksBelowAlertTriggeredWhenCurrentPriceIsBelowTarget() {
        User user = new User("user", "user@example.com", "hashed-password");
        PriceAlert alert = new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.BELOW,
                new BigDecimal("200.0000")
        );

        when(priceAlertRepository.findByStatusAndExpiresAtLessThanEqual(
                eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of());

        when(priceAlertRepository.findDistinctActiveSymbols(eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of("AAPL"));

        when(stockService.getStock("AAPL")).thenReturn(
                new StockQuoteDTO("AAPL", new BigDecimal("190.0000"), Instant.now()));

        when(priceAlertRepository.findByStatusAndSymbolAndExpiresAtAfter(
                eq(PriceAlertStatus.ACTIVE), eq("AAPL"), any()))
                .thenReturn(List.of(alert));

        priceAlertScheduler.checkPriceAlerts();

        assertEquals(PriceAlertStatus.TRIGGERED, alert.getStatus());
    }

    // Test that an alert stays ACTIVE when the current price does not meet the alert condition
    @Test
    void checkPriceAlerts_leavesAlertActiveWhenConditionIsNotMet() {
        User user = new User("user", "user@example.com", "hashed-password");
        PriceAlert alert = new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        );

        when(priceAlertRepository.findByStatusAndExpiresAtLessThanEqual(
                eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of());

        when(priceAlertRepository.findDistinctActiveSymbols(eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of("AAPL"));

        when(stockService.getStock("AAPL")).thenReturn(
                new StockQuoteDTO("AAPL", new BigDecimal("190.0000"), Instant.now()));

        when(priceAlertRepository.findByStatusAndSymbolAndExpiresAtAfter(
                eq(PriceAlertStatus.ACTIVE), eq("AAPL"), any()))
                .thenReturn(List.of(alert));

        priceAlertScheduler.checkPriceAlerts();

        assertEquals(PriceAlertStatus.ACTIVE, alert.getStatus());
    }

    // Test that expired ACTIVE alerts are marked EXPIRED
    @Test
    void checkPriceAlerts_marksExpiredActiveAlertsAsExpired() {
        User user = new User("user", "user@example.com", "hashed-password");
        PriceAlert expiredAlert = new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        );

        when(priceAlertRepository.findByStatusAndExpiresAtLessThanEqual(
                eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of(expiredAlert));

        when(priceAlertRepository.findDistinctActiveSymbols(eq(PriceAlertStatus.ACTIVE), any()))
                .thenReturn(List.of());

        priceAlertScheduler.checkPriceAlerts();

        assertEquals(PriceAlertStatus.EXPIRED, expiredAlert.getStatus());
    }
}

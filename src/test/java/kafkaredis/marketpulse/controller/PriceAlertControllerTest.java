package kafkaredis.marketpulse.controller;

import kafkaredis.marketpulse.dto.PriceAlertDTO;
import kafkaredis.marketpulse.entity.PriceAlertConditionType;
import kafkaredis.marketpulse.entity.PriceAlertStatus;
import kafkaredis.marketpulse.exception.GlobalExceptionHandler;
import kafkaredis.marketpulse.exception.PriceAlertAlreadyExistsException;
import kafkaredis.marketpulse.exception.UserNotFoundException;
import kafkaredis.marketpulse.service.PriceAlertService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PriceAlertControllerTest {

    private final PriceAlertService priceAlertService = mock(PriceAlertService.class);

    private final MockMvc mockMvc = standaloneSetup(new PriceAlertController(priceAlertService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    // Test that POST /api/users/{userId}/alerts returns the created price alert
    @Test
    void createPriceAlert_returnsCreatedAlert() throws Exception {
        when(priceAlertService.createPriceAlert(eq(1L), any()))
                .thenReturn(new PriceAlertDTO(
                        10L,
                        "AAPL",
                        PriceAlertConditionType.ABOVE,
                        new BigDecimal("200.0000"),
                        PriceAlertStatus.ACTIVE,
                        Instant.parse("2026-09-09T10:00:00Z"),
                        Instant.parse("2026-09-10T10:00:00Z")
                ));

        mockMvc.perform(post("/api/users/1/alerts")
                        .contentType("application/json")
                        .content("""
                                {
                                  "symbol": "AAPL",
                                  "conditionType": "ABOVE",
                                  "targetPrice": 200
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.conditionType").value("ABOVE"))
                .andExpect(jsonPath("$.targetPrice").value(200.0000))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // Test that GET /api/users/{userId}/alerts returns the user's price alerts
    @Test
    void getPriceAlerts_returnsUserAlerts() throws Exception {
        when(priceAlertService.getPriceAlerts(1L)).thenReturn(List.of(
                new PriceAlertDTO(
                        10L,
                        "AAPL",
                        PriceAlertConditionType.ABOVE,
                        new BigDecimal("200.0000"),
                        PriceAlertStatus.ACTIVE,
                        Instant.parse("2026-09-09T10:00:00Z"),
                        Instant.parse("2026-09-10T10:00:00Z")
                ),
                new PriceAlertDTO(
                        11L,
                        "TSLA",
                        PriceAlertConditionType.BELOW,
                        new BigDecimal("180.0000"),
                        PriceAlertStatus.ACTIVE,
                        Instant.parse("2026-09-09T11:00:00Z"),
                        Instant.parse("2026-09-10T11:00:00Z")
                )
        ));

        mockMvc.perform(get("/api/users/1/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].conditionType").value("ABOVE"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].symbol").value("TSLA"))
                .andExpect(jsonPath("$[1].conditionType").value("BELOW"));
    }

    // Test that duplicate price alerts return a 409 Conflict error response
    @Test
    void createPriceAlert_returns409WhenAlertAlreadyExists() throws Exception {
        when(priceAlertService.createPriceAlert(eq(1L), any()))
                .thenThrow(new PriceAlertAlreadyExistsException("Price alert already exists"));

        mockMvc.perform(post("/api/users/1/alerts")
                        .contentType("application/json")
                        .content("""
                                {
                                  "symbol": "AAPL",
                                  "conditionType": "ABOVE",
                                  "targetPrice": 200
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("PRICE_ALERT_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Price alert already exists"));
    }

    // Test that a missing user returns a 404 Not Found error response
    @Test
    void getPriceAlerts_returns404WhenUserDoesNotExist() throws Exception {
        when(priceAlertService.getPriceAlerts(99L))
                .thenThrow(new UserNotFoundException("User was not found"));

        mockMvc.perform(get("/api/users/99/alerts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User was not found"));
    }

    // Test that a blank symbol returns 400 before the service is called
    @Test
    void createPriceAlert_returns400WhenSymbolIsBlank() throws Exception {
        mockMvc.perform(post("/api/users/1/alerts")
                        .contentType("application/json")
                        .content("""
                                {
                                  "symbol": "",
                                  "conditionType": "ABOVE",
                                  "targetPrice": 200
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(priceAlertService, never()).createPriceAlert(any(), any());
    }

    // Test that a missing condition type returns 400 before the service is called
    @Test
    void createPriceAlert_returns400WhenConditionTypeIsMissing() throws Exception {
        mockMvc.perform(post("/api/users/1/alerts")
                        .contentType("application/json")
                        .content("""
                                {
                                  "symbol": "AAPL",
                                  "targetPrice": 200
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(priceAlertService, never()).createPriceAlert(any(), any());
    }

    // Test that a zero target price returns 400 before the service is called
    @Test
    void createPriceAlert_returns400WhenTargetPriceIsZero() throws Exception {
        mockMvc.perform(post("/api/users/1/alerts")
                        .contentType("application/json")
                        .content("""
                                {
                                  "symbol": "AAPL",
                                  "conditionType": "ABOVE",
                                  "targetPrice": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(priceAlertService, never()).createPriceAlert(any(), any());
    }
}

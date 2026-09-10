package kafkaredis.marketpulse.dto;

import kafkaredis.marketpulse.entity.PriceAlertConditionType;
import kafkaredis.marketpulse.entity.PriceAlertStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceAlertDTO(
        Long id,
        String symbol,
        PriceAlertConditionType conditionType,
        BigDecimal targetPrice,
        PriceAlertStatus status,
        Instant createdAt,
        Instant expiresAt
) {
}
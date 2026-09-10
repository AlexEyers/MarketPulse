package kafkaredis.marketpulse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kafkaredis.marketpulse.entity.PriceAlertConditionType;

import java.math.BigDecimal;

public record CreatePriceAlertRequestDTO(
        @NotBlank String symbol,

        @NotNull PriceAlertConditionType conditionType,

        @NotNull
        @DecimalMin(value = "0.0001")
        BigDecimal targetPrice
) {
}

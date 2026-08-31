package kafkaredis.marketpulse.dto;

import jakarta.validation.constraints.NotBlank;

public record AddWatchlistItemRequestDTO(
        @NotBlank String symbol
) {
}

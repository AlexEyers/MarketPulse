package kafkaredis.marketpulse.dto;

import java.time.Instant;

public record WatchlistItemDTO(
        Long id,
        String symbol,
        Instant createdAt
) {
}

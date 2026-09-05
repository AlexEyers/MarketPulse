package kafkaredis.marketpulse.dto;

import java.time.Instant;

public record UserDTO(
        Long id,
        String username,
        String email,
        Instant createdAt
) {
}

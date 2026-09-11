package kafkaredis.marketpulse.integration;

import kafkaredis.marketpulse.entity.PriceAlert;
import kafkaredis.marketpulse.entity.PriceAlertConditionType;
import kafkaredis.marketpulse.entity.PriceAlertStatus;
import kafkaredis.marketpulse.entity.User;
import kafkaredis.marketpulse.repository.PriceAlertRepository;
import kafkaredis.marketpulse.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Tests that PriceAlertRepository works correctly with the real PostgreSQL schema created by Flyway.
class PriceAlertRepositoryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PriceAlertRepository priceAlertRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Test that a PriceAlert can be saved and found by user ID
    @Test
    void saveAndFindPriceAlertsByUserId() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        priceAlertRepository.save(new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        ));

        List<PriceAlert> result = priceAlertRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals(PriceAlertConditionType.ABOVE, result.get(0).getConditionType());
        assertEquals(PriceAlertStatus.ACTIVE, result.get(0).getStatus());
        assertEquals(0, new BigDecimal("200.0000").compareTo(result.get(0).getTargetPrice()));
    }

    // Test that existsByUserIdAndSymbolAndConditionTypeAndTargetPriceAndStatus returns true when the PriceAlert exists
    @Test
    void existsByUserIdAndSymbolAndConditionTypeAndTargetPriceAndStatus_returnsTrueWhenAlertExists() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        priceAlertRepository.save(new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        ));

        boolean result = priceAlertRepository.existsByUserIdAndSymbolAndConditionTypeAndTargetPriceAndStatus(
                user.getId(),
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000"),
                PriceAlertStatus.ACTIVE
        );

        assertTrue(result);
    }

    // Test that findByUserIdOrderByCreatedAtDesc returns the newest PriceAlert first
    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsNewestAlertFirst() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        priceAlertRepository.save(new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        ));
        priceAlertRepository.save(new PriceAlert(
                user,
                "MSFT",
                PriceAlertConditionType.BELOW,
                new BigDecimal("300.0000")
        ));

        List<PriceAlert> result = priceAlertRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertEquals(2, result.size());
        assertEquals("MSFT", result.get(0).getSymbol());
        assertEquals("AAPL", result.get(1).getSymbol());
    }

    // Test that a new PriceAlert expires 24 hours after it is created
    @Test
    void newPriceAlertExpiresTwentyFourHoursAfterCreation() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        PriceAlert savedAlert = priceAlertRepository.save(new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        ));

        assertEquals(savedAlert.getCreatedAt().plus(24, ChronoUnit.HOURS), savedAlert.getExpiresAt());
    }

    // Test that duplicate ACTIVE PriceAlerts for the same user cannot be saved
    @Test
    void cannotSaveDuplicateActiveAlertForSameUser() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        priceAlertRepository.save(new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        ));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> priceAlertRepository.saveAndFlush(new PriceAlert(
                        user,
                        "AAPL",
                        PriceAlertConditionType.ABOVE,
                        new BigDecimal("200.0000")
                ))
        );
    }

    // Test that the same PriceAlert can be saved for different users
    @Test
    void canSaveSameAlertForDifferentUsers() {
        User firstUser = userRepository.save(new User(
                "user1",
                "user1@example.com",
                "hashed-password"
        ));
        User secondUser = userRepository.save(new User(
                "user2",
                "user2@example.com",
                "hashed-password"
        ));

        priceAlertRepository.save(new PriceAlert(
                firstUser,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        ));
        priceAlertRepository.save(new PriceAlert(
                secondUser,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        ));

        assertTrue(priceAlertRepository.existsByUserIdAndSymbolAndConditionTypeAndTargetPriceAndStatus(
                firstUser.getId(),
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000"),
                PriceAlertStatus.ACTIVE
        ));
        assertTrue(priceAlertRepository.existsByUserIdAndSymbolAndConditionTypeAndTargetPriceAndStatus(
                secondUser.getId(),
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000"),
                PriceAlertStatus.ACTIVE
        ));
    }

    // Test that findDistinctActiveSymbols returns each active, non-expired symbol once
    @Test
    void findDistinctActiveSymbols() {

        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        User user2 = userRepository.save(new User(
                "user2",
                "user2@example.com",
                "hashed-password"
        ));

        priceAlertRepository.save(new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        ));

        priceAlertRepository.save(new PriceAlert(
                user2,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        ));

        priceAlertRepository.save(new PriceAlert(
                user,
                "MSFT",
                PriceAlertConditionType.BELOW,
                new BigDecimal("300.0000")
        ));

        List<String> result = priceAlertRepository.findDistinctActiveSymbols(
                PriceAlertStatus.ACTIVE,
                Instant.now()
        );

        assertEquals(2, result.size());
        assertTrue(result.contains("AAPL"));
        assertTrue(result.contains("MSFT"));
    }

    // Test that findByStatusAndSymbolAndExpiresAtAfter returns active, non-expired alerts for 1 symbol
    @Test
    void findByStatusAndSymbolAndExpiresAtAfter() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        priceAlertRepository.save(new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        ));

        priceAlertRepository.save(new PriceAlert(
                user,
                "MSFT",
                PriceAlertConditionType.BELOW,
                new BigDecimal("300.0000")
        ));

        List<PriceAlert> result = priceAlertRepository.findByStatusAndSymbolAndExpiresAtAfter(
                PriceAlertStatus.ACTIVE,
                "AAPL",
                Instant.now()
        );

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals(PriceAlertStatus.ACTIVE, result.get(0).getStatus());
    }

    // Test that findByStatusAndExpiresAtLessThanEqual returns active alerts that have expired
    @Test
    void findByStatusAndExpiresAtLessThanEqual() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        Instant now = Instant.now();

        priceAlertRepository.save(new PriceAlert(
                user,
                "MSFT",
                PriceAlertConditionType.BELOW,
                new BigDecimal("300.0000")
        ));

        jdbcTemplate.update("""
                        INSERT INTO price_alerts (
                            user_id,
                            symbol,
                            condition_type,
                            target_price,
                            status,
                            created_at,
                            expires_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                user.getId(),
                "AAPL",
                "ABOVE",
                new BigDecimal("200.0000"),
                "ACTIVE",
                Timestamp.from(now.minus(25, ChronoUnit.HOURS)),
                Timestamp.from(now.minus(1, ChronoUnit.HOURS))
        );

        List<PriceAlert> result = priceAlertRepository.findByStatusAndExpiresAtLessThanEqual(
                PriceAlertStatus.ACTIVE,
                now
        );

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals(PriceAlertStatus.ACTIVE, result.get(0).getStatus());
    }
}

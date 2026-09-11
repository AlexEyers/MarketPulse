package kafkaredis.marketpulse.repository;

import kafkaredis.marketpulse.entity.PriceAlert;
import kafkaredis.marketpulse.entity.PriceAlertConditionType;
import kafkaredis.marketpulse.entity.PriceAlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    // Method to return to frontend a users tracked stocks (active,disabled, triggered, expired)
    List<PriceAlert> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Make sure a user is not creating a new tracking stock request that they already have
    // Better to throw an error earlier than waiting for it to attempt to be inserted into the DB then failing because of the unique constraint.
    boolean existsByUserIdAndSymbolAndConditionTypeAndTargetPriceAndStatus(
            Long userId,
            String symbol,
            PriceAlertConditionType conditionType,
            BigDecimal targetPrice,
            PriceAlertStatus status
    );

    // Return a List of distinct symbols that currently have at least 1 active, non-expired alert
    @Query("""
            SELECT DISTINCT p.symbol
            FROM PriceAlert p
            WHERE p.status = :status
            AND p.expiresAt > :now
            """)
    List<String> findDistinctActiveSymbols(
            PriceAlertStatus status,
            Instant now
    );

    // Returns a List of all active, non-expired alerts for 1 symbol
    List<PriceAlert> findByStatusAndSymbolAndExpiresAtAfter(
            PriceAlertStatus status,
            String symbol,
            Instant now
    );

    // Return a List of PriceAlert's that have expired
    List<PriceAlert> findByStatusAndExpiresAtLessThanEqual(
            PriceAlertStatus status,
            Instant now
    );
}

package kafkaredis.marketpulse.repository;

import kafkaredis.marketpulse.entity.PriceAlert;
import kafkaredis.marketpulse.entity.PriceAlertConditionType;
import kafkaredis.marketpulse.entity.PriceAlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
}

package kafkaredis.marketpulse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "price_alerts")
@Getter
@NoArgsConstructor
public class PriceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 15, nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", length = 20, nullable = false)
    private PriceAlertConditionType conditionType;

    @Column(name = "target_price", precision = 12, scale = 4, nullable = false)
    private BigDecimal targetPrice;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PriceAlertStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public PriceAlert(
            User user,
            String symbol,
            PriceAlertConditionType conditionType,
            BigDecimal targetPrice
    ) {
        this.user = user;
        this.symbol = symbol;
        this.conditionType = conditionType;
        this.targetPrice = targetPrice;
        this.status = PriceAlertStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plus(24, ChronoUnit.HOURS);
    }

    // Check if a PriceAlert should be triggered
    public boolean isTriggeredBy(BigDecimal currentPrice) {
        // If condition is ABOVE, then return true if the currentPrice is larger than the target price
        if(conditionType == PriceAlertConditionType.ABOVE) {
            return currentPrice.compareTo(targetPrice) >= 0;
        }
        // If condition is BELOW, then return true if the currentPrice is lower than the target price
        if(conditionType == PriceAlertConditionType.BELOW) {
            return currentPrice.compareTo(targetPrice) <= 0;
        }

        return false;
    }

    public void markTriggered() {
        this.status = PriceAlertStatus.TRIGGERED;
    }

    public void markExpired() {
        this.status = PriceAlertStatus.EXPIRED;
    }

    public void markDisabled() {
        this.status = PriceAlertStatus.DISABLED;
    }
}

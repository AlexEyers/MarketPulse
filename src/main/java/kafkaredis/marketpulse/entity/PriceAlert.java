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
}

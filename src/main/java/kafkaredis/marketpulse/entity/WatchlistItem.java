package kafkaredis.marketpulse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "watchlist_items")
@Getter
@NoArgsConstructor
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Many WatchlistItem objects belong to one User. Lazy load User as probabaly won't need it often
    @JoinColumn(name = "user_id", nullable = false) // Tells JPA the User relationship is stored using the user_id column in the table
    private User user;

    @Column(nullable = false, length = 15)
    private String symbol;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public WatchlistItem(User user, String symbol) {
        this.user = user;
        this.symbol = symbol;
        this.createdAt = Instant.now();
    }
}

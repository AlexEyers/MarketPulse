package kafkaredis.marketpulse.repository;

import kafkaredis.marketpulse.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {

    // List all watchlist items for a user
    List<WatchlistItem> findByUserIdOrderBySymbolAsc(Long userId);

    // Check if a symbol already exists for that user
    boolean existsByUserIdAndSymbol(Long userId, String symbol);

    // Return the WatchlistItem row/entity if it exists
    Optional<WatchlistItem> findByUserIdAndSymbol(Long userId, String symbol);

}

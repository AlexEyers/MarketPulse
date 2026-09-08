package kafkaredis.marketpulse.integration;

import kafkaredis.marketpulse.entity.User;
import kafkaredis.marketpulse.entity.WatchlistItem;
import kafkaredis.marketpulse.repository.UserRepository;
import kafkaredis.marketpulse.repository.WatchlistItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Tests that WatchlistItemRepository works correctly with the real PostgreSQL schema created by Flyway.
class WatchlistItemRepositoryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WatchlistItemRepository watchlistItemRepository;

    @Test
    void findByUserIdAndSymbol_returnsMatchingItem() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        watchlistItemRepository.save(new WatchlistItem(user, "AAPL"));

        Optional<WatchlistItem> result =
                watchlistItemRepository.findByUserIdAndSymbol(user.getId(), "AAPL");

        assertTrue(result.isPresent());
        assertEquals("AAPL", result.get().getSymbol());
        assertEquals(user.getId(), result.get().getUser().getId());
    }

    @Test
    void existsByUserIdAndSymbol_returnsTrueWhenItemExists() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        watchlistItemRepository.save(new WatchlistItem(user, "AAPL"));

        boolean result = watchlistItemRepository.existsByUserIdAndSymbol(user.getId(), "AAPL");

        assertTrue(result);
    }

    @Test
    void findByUserIdOrderBySymbolAsc_returnsItemsSortedBySymbol() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        watchlistItemRepository.save(new WatchlistItem(user, "MSFT"));
        watchlistItemRepository.save(new WatchlistItem(user, "AAPL"));
        watchlistItemRepository.save(new WatchlistItem(user, "GOOG"));

        List<WatchlistItem> result = watchlistItemRepository.findByUserIdOrderBySymbolAsc(user.getId());

        assertEquals(3, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals("GOOG", result.get(1).getSymbol());
        assertEquals("MSFT", result.get(2).getSymbol());
    }

    @Test
    void delete_removesPersistedItem() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        WatchlistItem item = watchlistItemRepository.save(new WatchlistItem(user, "AAPL"));

        watchlistItemRepository.delete(item);

        Optional<WatchlistItem> result =
                watchlistItemRepository.findByUserIdAndSymbol(user.getId(), "AAPL");

        assertTrue(result.isEmpty());
    }

    @Test
    void cannotSaveDuplicateSymbolForSameUser() {
        User user = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        watchlistItemRepository.save(new WatchlistItem(user, "AAPL"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> watchlistItemRepository.saveAndFlush(new WatchlistItem(user, "AAPL"))
        );
    }

    @Test
    void canSaveSameSymbolForDifferentUsers() {
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

        watchlistItemRepository.save(new WatchlistItem(firstUser, "AAPL"));
        watchlistItemRepository.save(new WatchlistItem(secondUser, "AAPL"));

        assertTrue(watchlistItemRepository.existsByUserIdAndSymbol(firstUser.getId(), "AAPL"));
        assertTrue(watchlistItemRepository.existsByUserIdAndSymbol(secondUser.getId(), "AAPL"));
    }
}

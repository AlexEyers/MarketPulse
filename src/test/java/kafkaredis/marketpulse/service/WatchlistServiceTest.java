package kafkaredis.marketpulse.service;

import kafkaredis.marketpulse.dto.WatchlistItemDTO;
import kafkaredis.marketpulse.entity.User;
import kafkaredis.marketpulse.entity.WatchlistItem;
import kafkaredis.marketpulse.exception.UserNotFoundException;
import kafkaredis.marketpulse.exception.WatchlistItemAlreadyExistsException;
import kafkaredis.marketpulse.exception.WatchlistItemNotFoundException;
import kafkaredis.marketpulse.repository.UserRepository;
import kafkaredis.marketpulse.repository.WatchlistItemRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WatchlistServiceTest {

    // Mock dependencies of WatchlistService
    private final UserRepository userRepository = mock(UserRepository.class);
    private final WatchlistItemRepository watchlistItemRepository = mock(WatchlistItemRepository.class);

    private final WatchlistService watchlistService = new WatchlistService(userRepository, watchlistItemRepository);

    // Test that adding a WatchlistItem to a user's watchlist saves the normalized symbol
    @Test
    void addWatchlistItem_savesNormalizedSymbol() {
        User user = new User("user", "user@example.com", "hashed-password");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(watchlistItemRepository.existsByUserIdAndSymbol(1L, "AAPL")).thenReturn(false);
        when(watchlistItemRepository.save(any(WatchlistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WatchlistItemDTO result = watchlistService.addWatchlistItem(1L, " aapl ");

        assertEquals("AAPL", result.symbol());
        verify(watchlistItemRepository).save(any(WatchlistItem.class));
    }

    // Test that adding a WatchlistItem to a user's watchlist when it already exists, throws a WatchlistItemAlreadyExistsException
    @Test
    void addWatchlistItem_throwsWhenSymbolAlreadyExists() {
        User user = new User("user", "user@example.com", "hashed-password");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user)); // Return user
        when(watchlistItemRepository.existsByUserIdAndSymbol(1L, "AAPL")).thenReturn(true); // Return true - Already exists

        // assertThrows needs an Executable: code that JUnit can run while checking for the exception.
        // The lambda is just the short way to provide that Executable rather than creating new Executable and then overriding its execute() method to contain watchlistService.addWatchlistItem(1L, "AAPL")
        assertThrows( // Check that the method threw the expected exception
                WatchlistItemAlreadyExistsException.class,
                () -> watchlistService.addWatchlistItem(1L, "AAPL")
        );
        // Check that the mocked repository never called save(...) because duplicate symbols should not be saved
        verify(watchlistItemRepository, never()).save(any(WatchlistItem.class));
    }

    // Test that adding a WatchlistItem to a user's watchlist fails if the user does not exist
    @Test
    void addWatchlistItem_throwsWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> watchlistService.addWatchlistItem(1L, "AAPL")
        );

        verify(watchlistItemRepository, never()).save(any(WatchlistItem.class));
    }

    // Test that deleting an existing symbol removes it from the watchlist
    @Test
    void deleteWatchlistItem_deletesExistingItem() {
        User user = new User("user", "user@example.com",  "hashed-password");
        WatchlistItem item = new WatchlistItem(user, "AAPL");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(watchlistItemRepository.findByUserIdAndSymbol(1L, "AAPL")).thenReturn(Optional.of(item));

        watchlistService.deleteWatchlistItem(1L, " aapl ");

        verify(watchlistItemRepository).delete(item);
    }

    // Test that deleting a missing symbol throws a WatchlistItemNotFoundException
    @Test
    void deleteWatchlistItem_throwsWhenItemDoesNotExist() {
        User user = new User("user", "user@example.com", "hashed-password");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(watchlistItemRepository.findByUserIdAndSymbol(1L, "AAPL")).thenReturn(Optional.empty());

        assertThrows(
                WatchlistItemNotFoundException.class,
                () -> watchlistService.deleteWatchlistItem(1L, " aapl")
        );

        verify(watchlistItemRepository, never()).delete(any(WatchlistItem.class));
    }

    // Test that getWatchlist returns the user's watchlist items as DTOs
    @Test
    void getWatchlist_returnsItemsForUser() {
        User user = new User("user", "user@example.com",  "hashed-password");

        WatchlistItem apple = new WatchlistItem(user, "AAPL");
        WatchlistItem microsoft = new WatchlistItem(user, "Microsoft");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(watchlistItemRepository.findByUserIdOrderBySymbolAsc(1L)).thenReturn(List.of(apple, microsoft));

        List<WatchlistItemDTO> result = watchlistService.getWatchlist(1L);

        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).symbol());
        assertEquals("Microsoft", result.get(1).symbol());
    }


    // Test that getWatchlist fails if the user does not exist
    @Test
    void getWatchlist_throwsWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows( // (ExpectedExceptionClass, codeToRun)
                UserNotFoundException.class,
                () -> watchlistService.getWatchlist(1L)
        );

        verify(watchlistItemRepository, never()).findByUserIdOrderBySymbolAsc(any());
    }
}
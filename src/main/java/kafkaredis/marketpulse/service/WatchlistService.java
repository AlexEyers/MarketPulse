package kafkaredis.marketpulse.service;

import kafkaredis.marketpulse.dto.WatchlistItemDTO;
import kafkaredis.marketpulse.entity.User;
import kafkaredis.marketpulse.entity.WatchlistItem;
import kafkaredis.marketpulse.exception.InvalidSymbolException;
import kafkaredis.marketpulse.exception.UserNotFoundException;
import kafkaredis.marketpulse.exception.WatchlistItemAlreadyExistsException;
import kafkaredis.marketpulse.exception.WatchlistItemNotFoundException;
import kafkaredis.marketpulse.repository.UserRepository;
import kafkaredis.marketpulse.repository.WatchlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private static final String SYMBOL_PATTERN = "[A-Z0-9.-]{1,15}";

    private final UserRepository userRepository;
    private final WatchlistItemRepository watchlistItemRepository;

    // Return a list of a user's watchlist symbols
    @Transactional(readOnly = true)
    public List<WatchlistItemDTO> getWatchlist(Long userId) {
        requireUser(userId);

        return watchlistItemRepository.findByUserIdOrderBySymbolAsc(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public WatchlistItemDTO addWatchlistItem(Long userId, String symbol) {
        User user = requireUser(userId);
        String normalizedSymbol = normalizeSymbol(symbol);

        if(watchlistItemRepository.existsByUserIdAndSymbol(userId, normalizedSymbol)) {
            throw new WatchlistItemAlreadyExistsException("Symbol already exists in watchlist");
        }
        // In Spring Data JPA, repository.save(entity) saves the entity and returns the saved entity. - Useful because then we will have id after saving
        WatchlistItem savedItem = watchlistItemRepository.save(new WatchlistItem(user, normalizedSymbol));

        return toDTO(savedItem);
    }

    @Transactional
    public void deleteWatchlistItem(Long userId, String symbol) {
        requireUser(userId);
        String normalizedSymbol = normalizeSymbol(symbol);

        // Return the existing row/entity first as we need its ID to delete it
        WatchlistItem existingItem = watchlistItemRepository.findByUserIdAndSymbol(userId, normalizedSymbol)
                .orElseThrow(() -> new WatchlistItemNotFoundException("Symbol was not found in watchlist"));

        watchlistItemRepository.delete(existingItem);
    }

    // Check that the user exists
    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User was not found"));
    }

    // Normalize the symbol
    private String normalizeSymbol(String symbol) {
        String normalizedSymbol = symbol.trim().toUpperCase();

        if(normalizedSymbol.isBlank()) {
            throw new InvalidSymbolException("Stock symbol must not be blank");
        }

        if(!normalizedSymbol.matches(SYMBOL_PATTERN)) {
            throw new InvalidSymbolException("Stock symbol format is invalid");
        }

        return normalizedSymbol;
    }

    // Convert WatchlistItem entity to DTO
    private WatchlistItemDTO toDTO(WatchlistItem watchlistItem) {
        return new WatchlistItemDTO(
                watchlistItem.getId(),
                watchlistItem.getSymbol(),
                watchlistItem.getCreatedAt()
        );
    }
}

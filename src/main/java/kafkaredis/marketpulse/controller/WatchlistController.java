package kafkaredis.marketpulse.controller;

import jakarta.validation.Valid;
import kafkaredis.marketpulse.dto.AddWatchlistItemRequestDTO;
import kafkaredis.marketpulse.dto.WatchlistItemDTO;
import kafkaredis.marketpulse.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/watchlist")
@RequiredArgsConstructor
@Validated
public class WatchlistController {

    private final WatchlistService watchlistService;

    // Get a user's watchlist
    @GetMapping
    public ResponseEntity<List<WatchlistItemDTO>> getWatchlist(@PathVariable Long userId) {
        return ResponseEntity.ok(watchlistService.getWatchlist(userId));
    }

    // Add a symbol to a watchlist
    @PostMapping
    public ResponseEntity<WatchlistItemDTO> addWatchlistItem(
            @PathVariable Long userId,
            @Valid @RequestBody AddWatchlistItemRequestDTO request
    ) {
        WatchlistItemDTO watchlistItem = watchlistService.addWatchlistItem(userId, request.symbol());

        return ResponseEntity.status(HttpStatus.CREATED).body(watchlistItem);
    }

    // Delete a symbol in a watchlist
    @DeleteMapping("/{symbol}")
    public ResponseEntity<Void> deleteWatchlistItem(
            @PathVariable Long userId,
            @PathVariable String symbol
    ) {
        watchlistService.deleteWatchlistItem(userId, symbol);

        return ResponseEntity.noContent().build();
    }
}

package kafkaredis.marketpulse.controller;

import kafkaredis.marketpulse.dto.WatchlistItemDTO;
import kafkaredis.marketpulse.exception.GlobalExceptionHandler;
import kafkaredis.marketpulse.exception.InvalidSymbolException;
import kafkaredis.marketpulse.exception.UserNotFoundException;
import kafkaredis.marketpulse.exception.WatchlistItemAlreadyExistsException;
import kafkaredis.marketpulse.service.WatchlistService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class WatchlistControllerTest {

    private final WatchlistService watchlistService = mock(WatchlistService.class);

    private final MockMvc mockMvc = standaloneSetup(new WatchlistController(watchlistService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    // Test that GET /api/users/{userId}/watchlist returns the user's watchlist
    @Test
    void getWatchlist_returnsWatchlist() throws Exception {
        when(watchlistService.getWatchlist(1L)).thenReturn(List.of(
                new WatchlistItemDTO(10L, "AAPL", Instant.parse("2026-08-30T10:00:00Z")),
                new WatchlistItemDTO(11L, "MSFT", Instant.parse("2026-08-30T11:00:00Z"))
        ));

        mockMvc.perform(get("/api/users/1/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].symbol").value("MSFT"));
    }

    // Test that POST /api/users/{userId}/watchlist returns the created watchlist item
    @Test
    void addWatchlistItem_returnsCreatedItem() throws Exception {
        when(watchlistService.addWatchlistItem(1L, "AAPL"))
                .thenReturn(new WatchlistItemDTO(10L, "AAPL", Instant.parse("2026-08-30T10:00:00Z")));

        mockMvc.perform(post("/api/users/1/watchlist")
                        .contentType("application/json")
                        .content("""
                                {
                                  "symbol": "AAPL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    // Test that DELETE /api/users/{userId}/watchlist/{symbol} returns 204 No Content
    @Test
    void deleteWatchlistItem_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1/watchlist/AAPL"))
                .andExpect(status().isNoContent());

        verify(watchlistService).deleteWatchlistItem(1L, "AAPL");
    }

    // Test that duplicate symbols return a 409 Conflict error response
    @Test
    void addWatchlistItem_returns409WhenSymbolAlreadyExists() throws Exception {
        when(watchlistService.addWatchlistItem(1L, "AAPL"))
                .thenThrow(new WatchlistItemAlreadyExistsException("Symbol already exists in watchlist"));

        mockMvc.perform(post("/api/users/1/watchlist")
                        .contentType("application/json")
                        .content("""
                                {
                                  "symbol": "AAPL"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("WATCHLIST_ITEM_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Symbol already exists in watchlist"));
    }

    // Test that a missing user returns a 404 Not Found error response
    @Test
    void getWatchlist_returns404WhenUserDoesNotExist() throws Exception {
        when(watchlistService.getWatchlist(99L))
                .thenThrow(new UserNotFoundException("User was not found"));

        mockMvc.perform(get("/api/users/99/watchlist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User was not found"));
    }

    // Test that an invalid symbol returns a 400 Bad Request error response
    @Test
    void addWatchlistItem_returns400WhenSymbolIsInvalid() throws Exception {
        when(watchlistService.addWatchlistItem(1L, "AAPL!"))
                .thenThrow(new InvalidSymbolException("Stock symbol format is invalid"));

        mockMvc.perform(post("/api/users/1/watchlist")
                        .contentType("application/json")
                        .content("""
                                {
                                  "symbol": "AAPL!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_SYMBOL"))
                .andExpect(jsonPath("$.message").value("Stock symbol format is invalid"));
    }
}

package kafkaredis.marketpulse.service;

import kafkaredis.marketpulse.dto.StockQuoteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockCacheService {

    private static final Duration CACHE_TTL = Duration.ofSeconds(60); // cache expiry = 60secs
    private static final String KEY_PREFIX = "stock:"; // All top-level keys start with stock: e.g. stock:AAPL

    private final RedisTemplate<String, StockQuoteDTO> redisTemplate;

    // Optional returns either a StockQuoteDTO wrapped inside Optional, or Optional.empty()
    public Optional<StockQuoteDTO> get(String symbol) {
        // Get cached value of the symbol, either StockQuoteDTO or null
        // If it is StockQuoteDTO, return Optional containing StockQuoteDTO
        // else, return Optional.empty()
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(symbol)));
    }

    public void put(String symbol, StockQuoteDTO quote) {
        // Save the key : symbol and value : quote
        // CACHE_TTL = auto delete the cached quote after 60secs
        redisTemplate.opsForValue().set(key(symbol), quote, CACHE_TTL);
    }

    private String key(String symbol) {
        // Build the Redis key by adding our prefix to the symbol
        return KEY_PREFIX + symbol; // AAPL -> stock:AAPL
    }
}

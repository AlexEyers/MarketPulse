package kafkaredis.marketpulse.config;

import kafkaredis.marketpulse.dto.StockQuoteDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

// Tells Spring how to create a RedisTemplate<String,StockQuoteDTO>
@Configuration // Class contains Spring Beans
public class RedisConfig {

    @Bean
    public RedisTemplate<String, StockQuoteDTO> stockQuoteRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, StockQuoteDTO> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory); // Use this Redis connection to talk to localhost:6379 (auto-configured from .yaml)
        template.setKeySerializer(new StringRedisSerializer()); // Store Redis keys as readable Strings
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(objectMapper, StockQuoteDTO.class)); // Store top-level Redis values as JSON and read them back specifically as StockQuoteDTO

        template.setHashKeySerializer(new StringRedisSerializer()); // Allow us to use a Redis key's key e.g. AAPL's price key to update its value safely (otherwise opsForHash() may store/read values in a format not expected)
        template.setHashValueSerializer(new JacksonJsonRedisSerializer<>(objectMapper, StockQuoteDTO.class)); // Store Redis hash values as JSON, and read them back specifically as StockQuoteDTO, allows us to change a Redis key's key value e.g. AAPL, price key can change 123.45 to 123.46 safely (otherwise opsForHash may store/read values in a format not expected)

        template.afterPropertiesSet(); // Complete RedisTemplate setup

        return template;
    }
}

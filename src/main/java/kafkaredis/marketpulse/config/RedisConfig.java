package kafkaredis.marketpulse.config;

import kafkaredis.marketpulse.dto.StockQuoteDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
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
        template.setValueSerializer(new GenericJacksonJsonRedisSerializer(objectMapper)); // Store StockQuoteDTO values as JSON. (Java object -> JSON in Redis and JSON -> Java object when read back)

        template.setHashKeySerializer(new StringRedisSerializer()); // Allow us to use a Redis key's key e.g. AAPL's price key to update its value safely.
        template.setHashValueSerializer(new GenericJacksonJsonRedisSerializer(objectMapper)); // Allow us to use a Redis key's key e.g. AAPL's price key to update its value safely.

        template.afterPropertiesSet(); // Complete RedisTemplate setup

        return template;
    }
}

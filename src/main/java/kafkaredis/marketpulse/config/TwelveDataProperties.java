package kafkaredis.marketpulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "twelve-data.api")
public record TwelveDataProperties (
    String baseUrl,
    String apiKey
    ) {
}

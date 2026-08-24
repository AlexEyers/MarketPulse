package kafkaredis.marketpulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TwelveDataClientConfig {

    @Bean
    public RestClient twelveDataRestClient(TwelveDataProperties twelveDataProperties) {
        return RestClient.create(twelveDataProperties.baseUrl());
    }
}

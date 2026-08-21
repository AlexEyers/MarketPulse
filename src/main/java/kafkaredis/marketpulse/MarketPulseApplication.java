package kafkaredis.marketpulse;

import kafkaredis.marketpulse.config.TwelveDataProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TwelveDataProperties.class)
public class MarketPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketPulseApplication.class, args);
    }

}

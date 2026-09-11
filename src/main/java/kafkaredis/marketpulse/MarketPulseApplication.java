package kafkaredis.marketpulse;

import kafkaredis.marketpulse.config.TwelveDataProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(TwelveDataProperties.class)
public class MarketPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketPulseApplication.class, args);
    }

}

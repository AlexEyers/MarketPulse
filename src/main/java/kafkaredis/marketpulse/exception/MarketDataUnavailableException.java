package kafkaredis.marketpulse.exception;

public class MarketDataUnavailableException extends RuntimeException {

    public MarketDataUnavailableException(String message) {
        super(message);
    }
}
